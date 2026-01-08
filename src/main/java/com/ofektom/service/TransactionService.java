package com.ofektom.service;

import com.ofektom.dto.request.TransactionRequest;
import com.ofektom.dto.request.TransferRequest;
import com.ofektom.dto.response.TransactionResponse;

/**
 * Service interface for transaction operations.
 * Defines contract for credit/debit transactions and wallet transfers.
 */
public interface TransactionService {
    TransactionResponse processTransaction(TransactionRequest request);
    TransactionResponse transfer(TransferRequest request);
}

