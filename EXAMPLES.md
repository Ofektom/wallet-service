# API Testing Examples

This document provides comprehensive cURL examples for testing all wallet service endpoints.

## Prerequisites

- Application running on `http://localhost:8080`
- PostgreSQL database configured and running

---

## Test Flow (Sequential)

### Test 1: Create Wallet 1 (Sender - with initial balance)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{
    "initialBalanceInMinorUnits": 100000
  }'
```

**Expected Response:** `201 Created`
```json
{
  "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
  "balanceInMinorUnits": 100000,
  "balanceInMajorUnits": 1000,
  "createdAt": "2026-01-08T10:47:38",
  "updatedAt": "2026-01-08T10:47:38"
}
```

**Action:** Save `walletId` as `WALLET_1_ID` for subsequent tests.

---

### Test 2: Create Wallet 2 (Receiver - zero balance)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Expected Response:** `201 Created`
```json
{
  "walletId": "f71742dc-6030-408a-aace-fd714bfcd4dd",
  "balanceInMinorUnits": 0,
  "balanceInMajorUnits": 0,
  "createdAt": "2026-01-08T10:56:40",
  "updatedAt": "2026-01-08T10:56:40"
}
```

**Action:** Save `walletId` as `WALLET_2_ID` for subsequent tests.

---

### Test 3: Get Wallet 1 (Verify creation)

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/wallets/090f3fa8-8ec7-4267-bfc4-6857a3dd6423
```

**Expected Response:** `200 OK`
```json
{
  "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
  "balanceInMinorUnits": 100000,
  "balanceInMajorUnits": 1000,
  "createdAt": "2026-01-08T10:47:38",
  "updatedAt": "2026-01-08T10:47:38"
}
```

---

### Test 4: Credit Transaction (Add money to Wallet 1)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "type": "CREDIT",
    "amountInMinorUnits": 50000,
    "idempotencyKey": "credit-key-001"
  }'
```

**Expected Response:** `201 Created`
```json
{
  "transactionId": "f2e2e981-d0d4-4ea0-8b24-88f0c24e7bc2",
  "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
  "transactionType": "CREDIT",
  "amountInMinorUnits": 50000,
  "amountInMajorUnits": 500,
  "createdAt": "2026-01-08T11:05:02"
}
```

**Verification:** Wallet 1 balance should now be 150000 (100000 + 50000)

---

### Test 5: Debit Transaction (Remove money from Wallet 1)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "type": "DEBIT",
    "amountInMinorUnits": 30000,
    "idempotencyKey": "debit-key-002"
  }'
```

**Expected Response:** `201 Created`
```json
{
  "transactionId": "c24798f5-957c-424a-b863-09c807f27fb0",
  "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
  "transactionType": "DEBIT",
  "amountInMinorUnits": 30000,
  "amountInMajorUnits": 300,
  "createdAt": "2026-01-08T11:08:12"
}
```

**Verification:** Wallet 1 balance should now be 120000 (150000 - 30000)

---

### Test 6: Test Idempotency (Duplicate Transaction)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "type": "CREDIT",
    "amountInMinorUnits": 50000,
    "idempotencyKey": "credit-key-001"
  }'
```

**Expected Response:** `409 Conflict`
```json
{
  "path": "/api/v1/transactions",
  "message": "Transaction with idempotency key already processed: credit-key-001",
  "statusCode": 409,
  "localDateTime": "2026-01-08T11:10:45.950988"
}
```

**Note:** Using the same idempotency key as Test 4 should return 409 Conflict.

---

### Test 7: Test Insufficient Balance (Debit more than available)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "type": "DEBIT",
    "amountInMinorUnits": 200000,
    "idempotencyKey": "debit-key-003"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "path": "/api/v1/transactions",
  "message": "Insufficient balance: Insufficient balance",
  "statusCode": 400,
  "localDateTime": "2026-01-08T11:13:51.908665"
}
```

**Note:** Trying to debit 200000 when balance is 120000 should fail.

---

### Test 8: Transfer from Wallet 1 to Wallet 2

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "senderWalletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "receiverWalletId": "f71742dc-6030-408a-aace-fd714bfcd4dd",
    "amountInMinorUnits": 40000,
    "idempotencyKey": "transfer-key-001"
  }'
```

**Expected Response:** `201 Created`
```json
{
  "transactionId": "10389327-8f2f-471b-a6d3-3796a02c0556",
  "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
  "transactionType": "DEBIT",
  "amountInMinorUnits": 40000,
  "amountInMajorUnits": 400,
  "createdAt": "2026-01-08T11:26:26"
}
```

**Verification:** 
- Wallet 1 balance should be 80000 (120000 - 40000)
- Wallet 2 balance should be 40000 (0 + 40000)

---

### Test 9: Verify Transfer - Get Wallet 1 (Sender)

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/wallets/090f3fa8-8ec7-4267-bfc4-6857a3dd6423
```

**Expected Response:** `200 OK`
```json
{
  "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
  "balanceInMinorUnits": 80000,
  "balanceInMajorUnits": 800,
  "createdAt": "2026-01-08T10:47:38",
  "updatedAt": "2026-01-08T11:26:28"
}
```

---

### Test 10: Verify Transfer - Get Wallet 2 (Receiver)

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/wallets/f71742dc-6030-408a-aace-fd714bfcd4dd
```

**Expected Response:** `200 OK`
```json
{
  "walletId": "f71742dc-6030-408a-aace-fd714bfcd4dd",
  "balanceInMinorUnits": 40000,
  "balanceInMajorUnits": 400,
  "createdAt": "2026-01-08T10:56:40",
  "updatedAt": "2026-01-08T11:26:28"
}
```

**Note:** This verifies the receiver wallet received the transfer amount.

---

### Test 11: Test Transfer Idempotency (Duplicate Transfer)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "senderWalletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "receiverWalletId": "f71742dc-6030-408a-aace-fd714bfcd4dd",
    "amountInMinorUnits": 40000,
    "idempotencyKey": "transfer-key-001"
  }'
```

**Expected Response:** `409 Conflict`
```json
{
  "path": "/api/v1/transfers",
  "message": "Transfer with idempotency key already processed: transfer-key-001",
  "statusCode": 409,
  "localDateTime": "2026-01-08T11:30:00"
}
```

**Note:** Using the same idempotency key as Test 8 should return 409 Conflict.

---

### Test 12: Test Wallet Not Found

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/wallets/non-existent-wallet-id
```

**Expected Response:** `404 Not Found`
```json
{
  "path": "/api/v1/wallets/non-existent-wallet-id",
  "message": "Wallet not found: non-existent-wallet-id",
  "statusCode": 404,
  "localDateTime": "2026-01-08T11:35:00"
}
```

---

### Test 13: Test Invalid Transaction Type

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "type": "INVALID_TYPE",
    "amountInMinorUnits": 1000,
    "idempotencyKey": "invalid-key-001"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "path": "/api/v1/transactions",
  "message": "Invalid transaction type: INVALID_TYPE",
  "statusCode": 400,
  "localDateTime": "2026-01-08T11:40:00"
}
```

---

### Test 14: Test Same Sender and Receiver (Transfer Validation)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "senderWalletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "receiverWalletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "amountInMinorUnits": 1000,
    "idempotencyKey": "same-wallet-key"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "path": "/api/v1/transfers",
  "message": "Sender and receiver wallets cannot be the same",
  "statusCode": 400,
  "localDateTime": "2026-01-08T11:45:00"
}
```

---

### Test 15: Test Validation Errors - Missing Required Fields

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "",
    "type": "",
    "amountInMinorUnits": -100
  }'
```

**Expected Response:** `422 Unprocessable Entity`
```json
{
  "status_code": 422,
  "error": "validation error",
  "detail": {
    "walletId": "Wallet ID is required",
    "type": "Transaction type (CREDIT/DEBIT) is required",
    "amountInMinorUnits": "Amount must be positive",
    "idempotencyKey": "Idempotency key is required"
  }
}
```

---

### Test 16: Test Transfer with Non-Existent Sender Wallet

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "senderWalletId": "non-existent-sender",
    "receiverWalletId": "f71742dc-6030-408a-aace-fd714bfcd4dd",
    "amountInMinorUnits": 1000,
    "idempotencyKey": "invalid-sender-key"
  }'
```

**Expected Response:** `404 Not Found`
```json
{
  "path": "/api/v1/transfers",
  "message": "Sender wallet not found: non-existent-sender",
  "statusCode": 404,
  "localDateTime": "2026-01-08T11:50:00"
}
```

---

### Test 17: Test Transfer with Non-Existent Receiver Wallet

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "senderWalletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "receiverWalletId": "non-existent-receiver",
    "amountInMinorUnits": 1000,
    "idempotencyKey": "invalid-receiver-key"
  }'
```

**Expected Response:** `404 Not Found`
```json
{
  "path": "/api/v1/transfers",
  "message": "Receiver wallet not found: non-existent-receiver",
  "statusCode": 404,
  "localDateTime": "2026-01-08T11:55:00"
}
```

---

### Test 18: Test Transfer with Insufficient Balance

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "senderWalletId": "090f3fa8-8ec7-4267-bfc4-6857a3dd6423",
    "receiverWalletId": "f71742dc-6030-408a-aace-fd714bfcd4dd",
    "amountInMinorUnits": 100000,
    "idempotencyKey": "insufficient-balance-key"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "path": "/api/v1/transfers",
  "message": "Insufficient balance. Current: 80000, Requested: 100000",
  "statusCode": 400,
  "localDateTime": "2026-01-08T12:00:00"
}
```

**Note:** Wallet 1 has 80000, trying to transfer 100000 should fail.

---

### Test 19: Test Transaction with Non-Existent Wallet

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "non-existent-wallet",
    "type": "CREDIT",
    "amountInMinorUnits": 1000,
    "idempotencyKey": "non-existent-wallet-key"
  }'
```

**Expected Response:** `404 Not Found`
```json
{
  "path": "/api/v1/transactions",
  "message": "Wallet not found: non-existent-wallet",
  "statusCode": 404,
  "localDateTime": "2026-01-08T12:05:00"
}
```

---

### Test 20: Test Create Wallet with Negative Initial Balance

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{
    "initialBalanceInMinorUnits": -1000
  }'
```

**Expected Response:** `422 Unprocessable Entity`
```json
{
  "status_code": 422,
  "error": "validation error",
  "detail": {
    "initialBalanceInMinorUnits": "Initial balance cannot be negative"
  }
}
```

---

## Test Summary

### ✅ Covered Tests (from your Postman tests):
1. Create Wallet 1 with initial balance
2. Create Wallet 2 with zero balance
3. Credit transaction
4. Debit transaction
5. Idempotency check (duplicate transaction)
6. Insufficient balance check
7. Transfer between wallets
8. Get wallet after transfer

### 📝 Additional Test Cases (Not Yet Covered):
9. Get Wallet 2 after transfer (verify receiver)
10. Transfer idempotency
11. Wallet not found (404)
12. Invalid transaction type
13. Same sender/receiver validation
14. Validation errors (missing fields)
15. Transfer with non-existent sender
16. Transfer with non-existent receiver
17. Transfer with insufficient balance
18. Transaction with non-existent wallet
19. Create wallet with negative balance

---

## Quick Reference

| Test # | Endpoint | Method | Purpose | Status Code |
|--------|----------|--------|---------|-------------|
| 1 | `/wallets` | POST | Create wallet with balance | 201 |
| 2 | `/wallets` | POST | Create wallet (zero) | 201 |
| 3 | `/wallets/{id}` | GET | Get wallet 1 | 200 |
| 4 | `/transactions` | POST | Credit transaction | 201 |
| 5 | `/transactions` | POST | Debit transaction | 201 |
| 6 | `/transactions` | POST | Test idempotency | 409 |
| 7 | `/transactions` | POST | Test insufficient balance | 400 |
| 8 | `/transfers` | POST | Transfer between wallets | 201 |
| 9 | `/wallets/{id}` | GET | Verify sender after transfer | 200 |
| 10 | `/wallets/{id}` | GET | Verify receiver after transfer | 200 |
| 11 | `/transfers` | POST | Test transfer idempotency | 409 |
| 12 | `/wallets/{id}` | GET | Test wallet not found | 404 |
| 13 | `/transactions` | POST | Test invalid transaction type | 400 |
| 14 | `/transfers` | POST | Test same sender/receiver | 400 |
| 15 | `/transactions` | POST | Test validation errors | 422 |
| 16 | `/transfers` | POST | Test non-existent sender | 404 |
| 17 | `/transfers` | POST | Test non-existent receiver | 404 |
| 18 | `/transfers` | POST | Test transfer insufficient balance | 400 |
| 19 | `/transactions` | POST | Test transaction with non-existent wallet | 404 |
| 20 | `/wallets` | POST | Test negative initial balance | 422 |

---

## Notes

- Replace wallet IDs in examples with actual IDs from your test responses
- All amounts are in minor units (kobo)
- Idempotency keys must be unique for each transaction
- Use the same idempotency key to test duplicate prevention
- Balance verification: After operations, use GET wallet to verify balances

