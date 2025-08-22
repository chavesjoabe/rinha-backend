package org.jchaves.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentDto(
    UUID correlationId,
    BigDecimal amount) {
}
