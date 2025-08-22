package org.jchaves.controller;

import org.jchaves.dto.CreatePaymentDto;
import org.jchaves.service.PaymentsService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("api/payments")
@ApplicationScoped
public class PaymentController {
  @Inject
  private PaymentsService paymentsService;

  @POST
  public Response create(CreatePaymentDto createPaymentDto) {
    var response = paymentsService.createPayment(createPaymentDto);
    return Response.ok(response).build();
  }

  @GET
  @Path("summary")
  public Response getPaymentSummary(
      @QueryParam("from") String from,
      @QueryParam("to") String to) {

    var response = paymentsService.getPaymentSummary(from, to);
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("all")
  public Response findAll() {
    var response = paymentsService.findAll();
    return Response.ok().entity(response).build();
  }
}
