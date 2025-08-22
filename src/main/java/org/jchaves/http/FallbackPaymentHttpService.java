package org.jchaves.http;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jchaves.dto.CreateExternalPaymentDto;
import org.jchaves.dto.CreateExternalPaymentSuccessDto;
import org.jchaves.dto.GetPaymentExternalServiceHealth;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "http://localhost:8002")
@Path("payments")
public interface FallbackPaymentHttpService {

  @GET
  @Path("service-health")
  @Produces(MediaType.APPLICATION_JSON)
  GetPaymentExternalServiceHealth getHealth();

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  CreateExternalPaymentSuccessDto createExternalPayment(CreateExternalPaymentDto externalPaymentDto);
}
