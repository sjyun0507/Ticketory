package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
