package com.ofektom.serviceImpl;

import com.ofektom.dto.request.TransactionRequest;
import com.ofektom.dto.request.TransferRequest;
import com.ofektom.dto.response.TransactionResponse;
import com.ofektom.enums.TransactionType;
import com.ofektom.exception.BadRequestException;
import com.ofektom.exception.ConflictException;
import com.ofektom.exception.NotFoundException;
import com.ofektom.model.IdempotencyKey;
import com.ofektom.model.Transaction;
import com.ofektom.model.Wallet;
import com.ofektom.repository.IdempotencyRepository;
import com.ofektom.repository.TransactionRepository;
import com.ofektom.repository.WalletRepository;
import com.ofektom.service.TransactionService;
import com.ofektom.utils.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRepository idempotencyRepository;

    @Autowired
    public TransactionServiceImpl(WalletRepository walletRepository, 
                                 TransactionRepository transactionRepository,
                                 IdempotencyRepository idempotencyRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse processTransaction(TransactionRequest request) {
        log.debug("Processing transaction: walletId={}, type={}, amount={}, idempotencyKey={}", 
            request.walletId(), request.type(), request.amountInMinorUnits(), request.idempotencyKey());
        
        // Idempotency check
        if (idempotencyRepository.existsByKeyValue(request.idempotencyKey())) {
            log.warn("Duplicate transaction attempt: idempotencyKey={}", request.idempotencyKey());
            throw new ConflictException("Transaction with idempotency key already processed: " + request.idempotencyKey());
        }
        
        // Find wallet with pessimistic lock for concurrent safety
        Wallet wallet = walletRepository.findByWalletIdWithLock(request.walletId())
            .orElseThrow(() -> {
                log.warn("Wallet not found for transaction: {}", request.walletId());
                return new NotFoundException("Wallet not found: " + request.walletId());
            });
        
        // Parse and validate transaction type
        TransactionType transactionType;
        try {
            transactionType = TransactionType.fromString(request.type());
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction type: {}", request.type());
            throw new BadRequestException("Invalid transaction type: " + request.type());
        }
        
        Money amount = Money.ofMinorUnits(request.amountInMinorUnits());
        
        // Process transaction using domain method
        try {
            wallet.processTransaction(transactionType, amount);
        } catch (IllegalStateException e) {
            log.error("Transaction failed: {}", e.getMessage());
            throw new BadRequestException("Insufficient balance: " + e.getMessage());
        }
        
        // Create transaction record
        Transaction transaction = Transaction.create(wallet, transactionType, amount);
        
        // Save idempotency key first (within transaction)
        try {
            idempotencyRepository.save(IdempotencyKey.of(request.idempotencyKey()));
        } catch (DataIntegrityViolationException e) {
            // Race condition - another thread already saved this key
            log.warn("Idempotency key already exists (race condition): {}", request.idempotencyKey());
            throw new ConflictException("Idempotency key already exists: " + request.idempotencyKey());
        }
        
        // Save wallet and transaction
        walletRepository.save(wallet);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Transaction processed successfully: transactionId={}, walletId={}, type={}, amount={}", 
            savedTransaction.getTransactionId(), wallet.getWalletId(), transactionType, amount);
        
        return mapToTransactionResponse(savedTransaction);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse transfer(TransferRequest request) {
        log.debug("Processing transfer: sender={}, receiver={}, amount={}, idempotencyKey={}", 
            request.senderWalletId(), request.receiverWalletId(), 
            request.amountInMinorUnits(), request.idempotencyKey());
        
        // Idempotency check
        if (idempotencyRepository.existsByKeyValue(request.idempotencyKey())) {
            log.warn("Duplicate transfer attempt: idempotencyKey={}", request.idempotencyKey());
            throw new ConflictException("Transfer with idempotency key already processed: " + request.idempotencyKey());
        }
        
        // Validate sender and receiver are different
        if (request.senderWalletId().equals(request.receiverWalletId())) {
            log.error("Sender and receiver wallets cannot be the same");
            throw new BadRequestException("Sender and receiver wallets cannot be the same");
        }
        
        // Find both wallets with pessimistic locks (atomic operation)
        Wallet sender = walletRepository.findByWalletIdWithLock(request.senderWalletId())
            .orElseThrow(() -> {
                log.warn("Sender wallet not found: {}", request.senderWalletId());
                return new NotFoundException("Sender wallet not found: " + request.senderWalletId());
            });
        
        Wallet receiver = walletRepository.findByWalletIdWithLock(request.receiverWalletId())
            .orElseThrow(() -> {
                log.warn("Receiver wallet not found: {}", request.receiverWalletId());
                return new NotFoundException("Receiver wallet not found: " + request.receiverWalletId());
            });
        
        Money amount = Money.ofMinorUnits(request.amountInMinorUnits());
        
        // Validate sufficient balance
        if (!sender.hasSufficientBalance(amount)) {
            log.warn("Insufficient balance for transfer: sender={}, balance={}, amount={}", 
                sender.getWalletId(), sender.getBalance(), amount);
            throw new BadRequestException(
                String.format("Insufficient balance. Current: %d, Requested: %d", 
                    sender.getBalance().getAmountInMinorUnits(), amount.getAmountInMinorUnits())
            );
        }
        
        // Atomic transfer - both operations in same transaction
        sender.debit(amount);
        receiver.credit(amount);
        
        // Save idempotency key
        try {
            idempotencyRepository.save(IdempotencyKey.of(request.idempotencyKey()));
        } catch (DataIntegrityViolationException e) {
            // Race condition - another thread already saved this key
            log.warn("Idempotency key already exists (race condition): {}", request.idempotencyKey());
            throw new ConflictException("Idempotency key already exists: " + request.idempotencyKey());
        }
        
        // Save both wallets
        walletRepository.save(sender);
        walletRepository.save(receiver);
        
        // Create transfer transaction record (debit transaction from sender's perspective)
        Transaction transfer = Transaction.create(sender, TransactionType.DEBIT, amount);
        Transaction savedTransfer = transactionRepository.save(transfer);
        
        log.info("Transfer completed successfully: transactionId={}, sender={}, receiver={}, amount={}", 
            savedTransfer.getTransactionId(), sender.getWalletId(), receiver.getWalletId(), amount);
        
        return mapToTransactionResponse(savedTransfer);
    }
    
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        Money amount = transaction.getAmount();
        return new TransactionResponse(
            transaction.getTransactionId(),
            transaction.getWallet().getWalletId(),
            transaction.getTransactionType().name(),
            amount.getAmountInMinorUnits(),
            amount.toMajorUnits(),
            transaction.getCreatedAt()
        );
    }
}

