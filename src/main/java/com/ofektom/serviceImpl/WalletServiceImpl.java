package com.ofektom.serviceImpl;

import com.ofektom.dto.request.CreateWalletRequest;
import com.ofektom.dto.response.WalletResponse;
import com.ofektom.exception.NotFoundException;
import com.ofektom.model.Wallet;
import com.ofektom.repository.WalletRepository;
import com.ofektom.service.WalletService;
import com.ofektom.utils.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for wallet operations.
 * Handles wallet creation and retrieval business logic.
 */
@Service
public class WalletServiceImpl implements WalletService {
    
    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private final WalletRepository walletRepository;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
    
    /**
     * Creates a new wallet with optional initial balance.
     * Balance defaults to zero if not specified.
     */
    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.debug("Creating new wallet");
        
        Wallet wallet = new Wallet();
        
        // Set initial balance if provided, otherwise defaults to 0 in @PrePersist
        if (request != null && request.initialBalanceInMinorUnits() != null) {
            wallet.setBalance(Money.ofMinorUnits(request.initialBalanceInMinorUnits()));
        }
        
        Wallet saved = walletRepository.save(wallet);
        
        log.info("Wallet created successfully: walletId={}, initialBalance={}", 
            saved.getWalletId(), saved.getBalanceInMinorUnits());
        
        return mapToWalletResponse(saved);
    }
    
    /**
     * Retrieves wallet details by wallet ID.
     * Throws NotFoundException if wallet does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(String walletId) {
        log.debug("Retrieving wallet: {}", walletId);
        
        return walletRepository.findByWalletId(walletId)
            .map(this::mapToWalletResponse)
            .orElseThrow(() -> {
                log.warn("Wallet not found: {}", walletId);
                return new NotFoundException("Wallet not found: " + walletId);
            });
    }
    
    private WalletResponse mapToWalletResponse(Wallet wallet) {
        Money balance = wallet.getBalance();
        return new WalletResponse(
            wallet.getWalletId(),
            balance.getAmountInMinorUnits(),
            balance.toMajorUnits(),
            wallet.getCreatedAt(),
            wallet.getUpdatedAt()
        );
    }
}
