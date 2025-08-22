package org.jchaves.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateExternalPaymentDto(
    UUID correlationId,
    BigDecimal amount,
    String requestAt) {
}
