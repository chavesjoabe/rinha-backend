package org.jchaves.dto;

import java.math.BigDecimal;

public record PaymentSummary(
    PaymentSummaryData main,
    PaymentSummaryData fallback) {

  public record PaymentSummaryData(
      Integer totalRequests,
      BigDecimal totalAmount) {
  }
}
