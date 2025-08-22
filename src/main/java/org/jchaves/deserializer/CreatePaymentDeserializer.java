package org.jchaves.deserializer;

import org.jchaves.dto.CreatePaymentDto;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class CreatePaymentDeserializer extends ObjectMapperDeserializer<CreatePaymentDto> {
  public CreatePaymentDeserializer() {
    super(CreatePaymentDto.class);
  }
}
