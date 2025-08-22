package org.jchaves.repository;

import java.time.Instant;
import java.util.List;

import org.jchaves.entity.Payment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentRepository implements PanacheRepository<Payment> {

  public List<Payment> findByCreatedAtBetween(Instant from, Instant to) {
    return list("createdAt BETWEEN ?1 AND ?2", from, to);
  }
}
