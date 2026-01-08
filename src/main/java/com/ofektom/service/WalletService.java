package com.ofektom.service;

import com.ofektom.dto.request.CreateWalletRequest;
import com.ofektom.dto.response.WalletResponse;

/**
 * Service interface for wallet operations.
 * Defines contract for wallet creation and retrieval.
 */
public interface WalletService {
    WalletResponse createWallet(CreateWalletRequest request);
    WalletResponse getWallet(String walletId);
}
