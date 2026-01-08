package com.ofektom.service;

import com.ofektom.dto.request.TransactionRequest;
import com.ofektom.dto.response.TransactionResponse;

public interface TransactionService {
    TransactionResponse processTransaction(TransactionRequest request);
}

