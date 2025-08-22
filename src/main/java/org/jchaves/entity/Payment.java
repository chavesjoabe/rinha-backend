package org.jchaves.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.jchaves.constants.PaymentProcessorEnum;
import org.jchaves.constants.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {
  @Id
  public String id;
  public UUID correlationId;
  public BigDecimal amount;
  public PaymentStatus status;
  public PaymentProcessorEnum processorType;
  public Instant createdAt;

  public Payment() {
  }

  public Payment(
      String id,
      UUID correlationId,
      BigDecimal amount,
      PaymentStatus status,
      PaymentProcessorEnum processorType,
      Instant createdAt) {
    this.id = id;
    this.correlationId = correlationId;
    this.amount = amount;
    this.status = status;
    this.processorType = processorType;
    this.createdAt = createdAt;
  }

}
