# Wallet Service API

A RESTful backend service for wallet operations built with Spring Boot 3.5.10 and Java 21.

## Overview

This service provides APIs for:
- Creating wallets
- Processing credit/debit transactions
- Transferring funds between wallets
- Retrieving wallet information

## Prerequisites

- JDK 21 or higher
- Maven 3.6+ (or use Maven wrapper: `./mvnw`)
- PostgreSQL 12+ (or Docker)

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE walletdb;
```

Or using Docker:

```bash
docker run --name postgres-wallet \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=walletdb \
  -p 5432:5432 \
  -d postgres:15
```

### 2. Configure Application

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/walletdb
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Alternatively, you can use environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/walletdb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

### 3. Build and Run

Using Maven wrapper:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

- **POST** `/api/v1/wallets` - Create a new wallet
- **GET** `/api/v1/wallets/{id}` - Get wallet details
- **POST** `/api/v1/transactions` - Credit or debit a wallet
- **POST** `/api/v1/transfers` - Transfer between two wallets

## Features

- Idempotent operations (prevents duplicate transactions)
- Atomic transfers (ACID compliance)
- Money stored in minor units (integer) to avoid floating-point errors
- Pessimistic locking for concurrent access safety
- Proper transaction isolation
- Input validation
- Comprehensive error handling

## Tech Stack

- Java 21
- Spring Boot 3.5.10
- PostgreSQL
- Maven

