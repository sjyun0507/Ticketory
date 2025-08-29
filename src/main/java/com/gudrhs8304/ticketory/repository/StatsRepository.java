package com.gudrhs8304.ticketory.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatsRepository {

    @PersistenceContext
    private EntityManager em;

    /** 승인(PAID) 매출 합계 */
    public BigDecimal sumApprovedAmount(LocalDate from, LocalDate to) {
        Query q = em.createNativeQuery("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM payment p
            WHERE p.status = 'PAID'
              AND p.paid_at >= :from
              AND p.paid_at < DATE_ADD(:to, INTERVAL 1 DAY)
        """);
        q.setParameter("from", from);
        q.setParameter("to", to);
        Object v = q.getSingleResult();
        return toBigDecimal(v);
    }

    /** 승인(PAID) 결제 건수 */
    public long countApprovedPayments(LocalDate from, LocalDate to) {
        Query q = em.createNativeQuery("""
            SELECT COUNT(*)
            FROM payment p
            WHERE p.status = 'PAID'
              AND p.paid_at >= :from
              AND p.paid_at < DATE_ADD(:to, INTERVAL 1 DAY)
        """);
        q.setParameter("from", from);
        q.setParameter("to", to);
        Object v = q.getSingleResult();
        return ((Number) v).longValue();
    }

    /** 환불 합계 */
    public BigDecimal sumRefundAmount(LocalDate from, LocalDate to) {
        Query q = em.createNativeQuery("""
            SELECT COALESCE(SUM(r.refund_amount), 0)
            FROM refund_log r
            JOIN payment p ON p.payment_id = r.payment_id
            WHERE r.status = 'SUCCESS'
              AND r.created_at >= :from
              AND r.created_at < DATE_ADD(:to, INTERVAL 1 DAY)
        """);
        q.setParameter("from", from);
        q.setParameter("to", to);
        Object v = q.getSingleResult();
        return toBigDecimal(v);
    }

    /** 일자별 승인(PAID) 매출 */
    @SuppressWarnings("unchecked")
    public List<Object[]> dailyApprovedRevenue(LocalDate from, LocalDate to) {
        Query q = em.createNativeQuery("""
            SELECT DATE(p.paid_at) AS d, COALESCE(SUM(p.amount),0) AS revenue
            FROM payment p
            WHERE p.status = 'PAID'
              AND p.paid_at >= :from
              AND p.paid_at < DATE_ADD(:to, INTERVAL 1 DAY)
            GROUP BY DATE(p.paid_at)
            ORDER BY d
        """);
        q.setParameter("from", from);
        q.setParameter("to", to);
        List<Object[]> rows = q.getResultList();

        List<Object[]> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) out.add(new Object[]{ r[0], r[1] });
        return out;
    }

    /** 승인(PAID) 기준 Top-N 영화 */
    @SuppressWarnings("unchecked")
    public List<Object[]> topMovies(LocalDate from, LocalDate to, int limit) {
        Query q = em.createNativeQuery("""
            SELECT m.movie_id              AS movieId,
                   m.title                 AS title,
                   COALESCE(SUM(p.amount),0) AS revenue
            FROM payment p
            JOIN booking   b ON b.booking_id   = p.booking_id
            JOIN screening s ON s.screening_id = b.screening_id
            JOIN movie     m ON m.movie_id     = s.movie_id
            WHERE p.status = 'PAID'
              AND p.paid_at >= :from
              AND p.paid_at < DATE_ADD(:to, INTERVAL 1 DAY)
            GROUP BY m.movie_id, m.title
            ORDER BY revenue DESC
            LIMIT :limit
        """);
        q.setParameter("from", from);
        q.setParameter("to", to);
        q.setParameter("limit", limit);

        List<Object[]> rows = q.getResultList();
        List<Object[]> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) out.add(new Object[]{ r[0], r[1], r[2] });
        return out;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        return new BigDecimal(String.valueOf(v));
    }
}