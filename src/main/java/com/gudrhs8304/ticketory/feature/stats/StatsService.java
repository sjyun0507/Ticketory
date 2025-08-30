package com.gudrhs8304.ticketory.feature.stats;

import com.gudrhs8304.ticketory.feature.stats.dto.DailyRevenueRes;
import com.gudrhs8304.ticketory.feature.stats.dto.SummaryRes;
import com.gudrhs8304.ticketory.feature.stats.dto.TopMovieRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final StatsRepository repo;

    public SummaryRes getSummary(LocalDate from, LocalDate to) {
        BigDecimal gross = repo.sumApprovedAmount(from, to);
        BigDecimal refunded = repo.sumRefundAmount(from, to);
        long count = repo.countApprovedPayments(from, to);
        BigDecimal net = gross.subtract(refunded);

        return new SummaryRes(gross, refunded, net, count);
    }

    public List<DailyRevenueRes> getDailyRevenue(LocalDate from, LocalDate to) {
        List<Object[]> rows = repo.dailyApprovedRevenue(from, to);
        List<DailyRevenueRes> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            // r[0]: java.sql.Date → LocalDate, r[1]: BigDecimal (또는 BigInteger/Long)
            LocalDate d = (r[0] instanceof java.sql.Date sqlDate) ? sqlDate.toLocalDate() : (LocalDate) r[0];
            BigDecimal v = (r[1] instanceof BigDecimal bd) ? bd : new BigDecimal(String.valueOf(r[1]));
            out.add(new DailyRevenueRes(d, v));
        }
        return out;
    }

    public List<TopMovieRes> getTopMovies(LocalDate from, LocalDate to, int limit) {
        List<Object[]> rows = repo.topMovies(from, to, limit);
        List<TopMovieRes> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long movieId = ((Number) r[0]).longValue();
            String title = (String) r[1];
            BigDecimal revenue = (r[2] instanceof BigDecimal bd) ? bd : new BigDecimal(String.valueOf(r[2]));
            out.add(new TopMovieRes(movieId, title, revenue));
        }
        return out;
    }
}