# rinha-backend

# Payments Service

This project is a **payment processing service** built with **Java + Quarkus**, using **Kafka** for message-driven communication and a resilient **payment processor integration** strategy.

---

## 📌 Overview

The service exposes REST endpoints to create and query payments. Payments are first **published to Kafka**, then asynchronously processed by a **payment processor service**. If the default processor fails, a **fallback processor** is used. If both fail, the payment is stored in the database with a **failed status**.

---

## ⚙️ Architecture

### Flow:

1. **API Request → PaymentController → PaymentsService**

<img width="872" height="421" alt="image" src="https://github.com/user-attachments/assets/2578ca54-3a80-4c20-9ad1-927822f9ecf1" />

   * Receives a request to create a payment.
   * Publishes the payment data to Kafka (`payments-out`).
   * Returns `"OK"` immediately.

3. **Message Processing → PaymentProcessorService**
<img width="1396" height="435" alt="image" src="https://github.com/user-attachments/assets/4edffce8-64f5-4724-9be3-750763b4c114" />

   * Listens to Kafka (`payments-in`).
   * Converts the message payload into a `CreateExternalPaymentDto`.
   * Attempts to process the payment using the **Default Processor**.
   * If retries fail, it attempts the **Fallback Processor**.
   * If all processors fail, the payment is stored with a **FAILED** status.

4. **Database (PostgreSQL/MongoDB)**

   * All processed payments are stored with details: correlationId, amount, processor type, status, and timestamp.

---

## 🏗️ Project Structure

```
org.jchaves.controller
 └── PaymentController.java       # REST API controller

org.jchaves.service
 ├── PaymentsService.java         # Handles API requests, publishes Kafka messages
 └── PaymentProcessorService.java # Consumes Kafka messages, processes payments

org.jchaves.repository
 └── PaymentRepository.java       # Data persistence

org.jchaves.dto
 ├── CreatePaymentDto.java        # DTO for creating payments
 ├── CreateExternalPaymentDto.java # DTO for external processors
 └── PaymentSummary.java          # Summary DTO for reports

org.jchaves.constants
 ├── PaymentProcessorEnum.java    # Processor types: DEFAULT, FALLBACK, NONE
 └── PaymentStatus.java           # Payment status: SUCCESS, FAIL
```

---

## 🚀 REST API Endpoints

### Create Payment

```http
POST /api/payments
Content-Type: application/json

{
  "correlationId": "12345",
  "amount": 100.50
}
```

**Response:**

```json
"OK"
```

### Get Payment Summary

```http
GET /api/payments/summary?from=2025-01-01T00:00:00Z&to=2025-01-31T23:59:59Z
```

**Response:**

```json
{
  "main": {
    "count": 10,
    "totalAmount": 2000.00
  },
  "fallback": {
    "count": 2,
    "totalAmount": 400.00
  }
}
```

### List All Payments

```http
GET /api/payments/all
```

Returns all stored payments.

---

## 📡 Kafka Integration

* **Producer:**

  * Channel: `payments-out`
  * Publishes `CreatePaymentDto` as JSON

* **Consumer:**

  * Channel: `payments-in`
  * Consumes JSON payloads

---

## 🔄 Retry & Fallback Strategy

* Default processor: retried **3 times**
* If all retries fail → fallback processor is used
* If fallback also fails → payment stored as **FAILED** with `NONE` processor type

---

## 🗄️ Database Entity

```java
public class Payment {
    String id;
    String correlationId;
    BigDecimal amount;
    PaymentStatus status;       // SUCCESS, FAIL
    PaymentProcessorEnum processorType; // DEFAULT, FALLBACK, NONE
    Instant createdAt;
}
```

---

## 📋 Example Logs

```bash
INFO  payload converted with success
INFO  sending request to default payment processor
INFO  PAYMENT CREATED IN DEFAULT PROCESSOR SUCCESSFULLY
```

If failures occur:

```bash
INFO  ERROR ON CREATE PAYMENT IN EXTERNAL SERVICE - RETRYING...
INFO  RETRY NUMBER - 1 OF 3
INFO  sending request to fallback payment processor
INFO  PAYMENT CREATED IN FALLBACK PROCESSOR SUCCESSFULLY
```

---

## ✅ Summary

This service provides:

* Kafka-based payment queuing and processing.
* REST API endpoints to create and query payments.
* Resilient processor handling with retries and fallbacks.
* Persistent storage of all payment operations.
* Query and reporting capabilities for payments.

---

## 🧑‍💻 Author

Developed by **Joabe Chaves** ✨

