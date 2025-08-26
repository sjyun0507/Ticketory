package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;   // ✅ 여기! lettuce 말고 spring-data Param
import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ---- 조회 ----
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
    Optional<Payment> findTopByBooking_BookingIdOrderByPaymentIdDesc(Long bookingId);

    // PESSIMISTIC WRITE - orderId 기준 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.orderId = :orderId")
    Optional<Payment> findByOrderIdForUpdate(@Param("orderId") String orderId);

    // PESSIMISTIC WRITE - bookingId 기준 PENDING 1건 잠금 (필요시 사용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select p from Payment p
            where p.booking.bookingId = :bookingId
              and p.status = com.gudrhs8304.ticketory.domain.enums.PaymentStatus.PENDING
              and p.paidAt is null
           """)
    Optional<Payment> findPendingByBookingIdForUpdate(@Param("bookingId") Long bookingId);

    // ---- 갱신 ----
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Payment p
              set p.orderId = :orderId
            where p.booking.bookingId = :bookingId
              and p.status = com.gudrhs8304.ticketory.domain.enums.PaymentStatus.PENDING
              and p.paidAt is null
           """)
    int attachOrderIdToPendingByBookingId(@Param("bookingId") Long bookingId,
                                          @Param("orderId") String orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Payment p set p.amount = :amount where p.orderId = :orderId")
    int updateAmountByOrderId(@Param("orderId") String orderId,
                              @Param("amount") BigDecimal amount);
}