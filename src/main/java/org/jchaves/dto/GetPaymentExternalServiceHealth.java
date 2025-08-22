package org.jchaves.dto;

public record GetPaymentExternalServiceHealth(
    boolean failing,
    Integer minResponseTime) {
}
