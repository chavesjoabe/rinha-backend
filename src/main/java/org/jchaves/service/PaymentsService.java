package org.jchaves.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jchaves.constants.PaymentProcessorEnum;
import org.jchaves.dto.CreatePaymentDto;
import org.jchaves.dto.PaymentSummary;
import org.jchaves.dto.PaymentSummary.PaymentSummaryData;
import org.jchaves.entity.Payment;
import org.jchaves.http.DefaultPaymentHttpService;
import org.jchaves.repository.PaymentRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentsService {

  @Inject
  @Channel("payments-out")
  private Emitter<String> emitter;

  @Inject
  private ObjectMapper objectMapper;

  @RestClient
  private DefaultPaymentHttpService defaultPaymentHttpService;

  @Inject
  private PaymentRepository paymentRepository;

  public String createPayment(CreatePaymentDto paymentDto) {
    try {
      String payload = objectMapper.writeValueAsString(paymentDto);
      emitter.send(payload);
      return "OK";
    } catch (Exception exception) {
      exception.printStackTrace();
      throw new RuntimeException("Error on send payment to queue");
    }
  }

  public List<Payment> findAll() {
    return paymentRepository.listAll();
  }

  public List<Payment> findByDates(String from, String to) {
    Instant fromDate = Instant.parse(from);
    Instant toDate = Instant.parse(to);

    return paymentRepository.findByCreatedAtBetween(fromDate, toDate);
  }

  public PaymentSummary getPaymentSummary(String from, String to) {
    List<Payment> payments = findByDates(from, to);

    List<Payment> mainPayments = payments.stream()
        .filter(payment -> payment.processorType == PaymentProcessorEnum.DEFAULT).toList();

    BigDecimal mainPaymentsTotalAmount = getPaymentTotalAmount(mainPayments);

    List<Payment> fallbackPayments = payments.stream()
        .filter(payment -> payment.processorType == PaymentProcessorEnum.FALLBACK).toList();

    BigDecimal fallbackPaymentsTotalAmount = getPaymentTotalAmount(fallbackPayments);

    PaymentSummaryData mainSummaryData = new PaymentSummaryData(
        mainPayments.size(),
        mainPaymentsTotalAmount);

    PaymentSummaryData fallbackSummaryData = new PaymentSummaryData(
        fallbackPayments.size(),
        fallbackPaymentsTotalAmount);

    return new PaymentSummary(mainSummaryData, fallbackSummaryData);
  }

  public BigDecimal getPaymentTotalAmount(List<Payment> payments) {
    return payments
        .stream()
        .map(item -> item.amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
