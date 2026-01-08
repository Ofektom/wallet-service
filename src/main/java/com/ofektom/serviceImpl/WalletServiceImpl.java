package com.ofektom.serviceImpl;

import com.ofektom.dto.request.CreateWalletRequest;
import com.ofektom.dto.response.WalletResponse;
import com.ofektom.model.Wallet;
import com.ofektom.repository.WalletRepository;
import com.ofektom.service.WalletService;
import com.ofektom.utils.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletServiceImpl implements WalletService {
    
    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private final WalletRepository walletRepository;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
    
    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.debug("Creating new wallet");
        
        Wallet wallet = new Wallet();
        
        Wallet saved = walletRepository.save(wallet);
        
        log.info("Wallet created successfully: walletId={}", saved.getWalletId());
        
        return mapToWalletResponse(saved);
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
