package com.ofektom.service;

import com.ofektom.dto.request.CreateWalletRequest;
import com.ofektom.dto.response.WalletResponse;

public interface WalletService {
    WalletResponse createWallet(CreateWalletRequest request);
}
