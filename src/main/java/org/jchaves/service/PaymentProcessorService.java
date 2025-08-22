package org.jchaves.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jchaves.config.ExecutorConfig;
import org.jchaves.constants.PaymentProcessorEnum;
import org.jchaves.constants.PaymentStatus;
import org.jchaves.dto.CreateExternalPaymentDto;
import org.jchaves.dto.CreatePaymentDto;
import org.jchaves.entity.Payment;
import org.jchaves.http.DefaultPaymentHttpService;
import org.jchaves.http.FallbackPaymentHttpService;
import org.jchaves.repository.PaymentRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PaymentProcessorService {

  @Inject
  ObjectMapper objectMapper;

  @RestClient
  private DefaultPaymentHttpService defaultPaymentHttpService;

  @RestClient
  private FallbackPaymentHttpService fallbackPaymentHttpService;

  @Inject
  private PaymentRepository paymentRepository;

  @Inject
  private ExecutorConfig executorConfig;

  private static final Logger logger = Logger.getLogger(PaymentProcessorService.class);

  @Incoming("payments-in")
  public void listen(String message) {
    executorConfig.getExecutor().submit(() -> processPayment(message));
  }

  @Transactional
  public void processPayment(String message) {
    CreateExternalPaymentDto payload;

    try {
      CreatePaymentDto paymentDto = objectMapper.readValue(message, CreatePaymentDto.class);
      payload = new CreateExternalPaymentDto(
          paymentDto.correlationId(),
          paymentDto.amount(),
          LocalDate.now().toString());

      logger.info("payload converted with success");
    } catch (Exception exception) {
      String errorMessage = "ERROR ON CONVERT PAYLOAD FROM KAFKA";
      logger.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }

    Integer MAX_RETRY_ATTEMPTS = 3;
    for (Integer attempt = 0; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
      try {
        logger.info("sending request to default payment processor");
        defaultPaymentHttpService.createExternalPayment(payload);

        createPaymentInDatabase(
            payload,
            PaymentProcessorEnum.DEFAULT,
            PaymentStatus.SUCCESS);

        logger.info("PAYMENT CREATED IN DEFAULT PROCESSOR SUCCESSFULLY");
        return;
      } catch (Exception exception) {
        exception.printStackTrace();
        logger.info("ERROR ON CREATE PAYMENT IN EXTERNAL SERVICE - RETRYING...");
        logger.infof("RETRY NUMBER - %n OF %n", attempt, MAX_RETRY_ATTEMPTS);
      }
    }

    try {
      logger.info("sending request to fallback payment processor");
      fallbackPaymentHttpService.createExternalPayment(payload);

      createPaymentInDatabase(
          payload,
          PaymentProcessorEnum.FALLBACK,
          PaymentStatus.SUCCESS);

      logger.info("PAYMENT CREATED IN FALLBACK PROCESSOR SUCCESSFULLY");

    } catch (Exception exception) {
      String errorMessage = "ERROR ON CREATE PAYMENT IN ALL SERVICES";
      logger.error(errorMessage);
      createPaymentInDatabase(
          payload,
          PaymentProcessorEnum.NONE,
          PaymentStatus.FAIL);

      logger.info("PAYMENT CREATED IN DATABASE IN FAILED STATUS, NO PROCESSOR AVAILABLE TO PROCESS THE PAYMENT");
    }
  }

  public void createPaymentInDatabase(CreateExternalPaymentDto paymentDto, PaymentProcessorEnum processorEnum,
      PaymentStatus status) {

    Payment paymentToSave = new Payment(
        UUID.randomUUID().toString(),
        paymentDto.correlationId(),
        paymentDto.amount(),
        status,
        PaymentProcessorEnum.DEFAULT,
        Instant.now());

    paymentRepository.persist(paymentToSave);
  }
}
