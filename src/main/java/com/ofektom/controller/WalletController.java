package com.ofektom.controller;

import com.ofektom.dto.request.CreateWalletRequest;
import com.ofektom.dto.response.WalletResponse;
import com.ofektom.service.WalletService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for wallet operations.
 * Handles wallet creation and retrieval endpoints.
 */
@RestController
@RequestMapping("/api/v1")
public class WalletController {
    
    private static final Logger log = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;
    
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }
    
    @PostMapping("/wallets")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        log.info("POST /wallets - Creating new wallet");
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/wallets/{id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String id) {
        log.info("GET /wallets/{} - Retrieving wallet", id);
        WalletResponse response = walletService.getWallet(id);
        return ResponseEntity.ok(response);
    }
}
