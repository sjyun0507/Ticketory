package com.gudrhs8304.ticketory.feature.booking;


import com.gudrhs8304.ticketory.core.jwt.JwtTokenProvider;
import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.booking.domain.BookingSeat;
import com.gudrhs8304.ticketory.feature.member.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.member.enums.PricingKind;
import com.gudrhs8304.ticketory.feature.member.enums.PricingOp;
import com.gudrhs8304.ticketory.feature.booking.dto.CreateBookingRequest;
import com.gudrhs8304.ticketory.feature.booking.dto.CreateBookingResponse;
import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.pricing.PricingRuleRepository;
import com.gudrhs8304.ticketory.feature.pricing.domain.PricingRule;
import com.gudrhs8304.ticketory.feature.screen.domain.Seat;
import com.gudrhs8304.ticketory.feature.screening.ScreeningRepository;
import com.gudrhs8304.ticketory.feature.screening.SeatHoldRepository;
import com.gudrhs8304.ticketory.feature.screening.SeatRepository;
import com.gudrhs8304.ticketory.feature.screening.domain.Screening;
import com.gudrhs8304.ticketory.feature.pricing.PricingService;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final PricingService pricingService;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SeatHoldRepository seatHoldRepository;
    private final PricingRuleRepository pricingRuleRepository;

    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("12000");

    @Transactional
    public CreateBookingResponse create(Long memberId, CreateBookingRequest req) {
        Screening screening = screeningRepository.findById(req.screeningId())
                .orElseThrow(() -> new IllegalArgumentException("상영이 없습니다."));

        List<Seat> seats = seatRepository.findBySeatIdIn(req.seatIds());
        if (seats.size() != req.seatIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 좌석이 포함되어 있습니다.");
        }

        Long screenId = screening.getScreen().getScreenId();
        boolean allSameScreen = seats.stream()
                .allMatch(s -> s.getScreen().getScreenId().equals(screenId));
        if (!allSameScreen) {
            throw new IllegalArgumentException("다른 상영관 좌석이 섞여 있습니다.");
        }

        // 이미 예약된 좌석 방지
        for (Long seatId : req.seatIds()) {
            if (bookingSeatRepository
                    .existsByScreening_ScreeningIdAndSeat_SeatId(req.screeningId(), seatId)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다. (seatId=" + seatId + ")");
            }
        }

        // ==== 가격 계산 (pricing_rule 적용) ====
        var now = LocalDateTime.now();
        // 카운트(미지정 시 0)
        int cntAdult = req.adult() != null ? req.adult() : req.seatIds().size();
        int cntTeen  = req.teen() != null ? req.teen() : 0;

        BigDecimal total = pricingService.computeTotal(screenId, cntAdult, cntTeen, now);

        // 기본 단가(상영관 base_price → 없으면 DEFAULT)
        BigDecimal baseUnit = Optional.ofNullable(screening.getScreen().getBasePrice())
                .map(i -> new BigDecimal(i))
                .orElse(DEFAULT_UNIT_PRICE);

        // 활성화된 규칙 로드
        List<PricingRule> rules = pricingRuleRepository.findActiveByScreenId(screenId, now);

        // 인원 유형별 단가 계산
        BigDecimal adultUnit = applyRules(baseUnit, rules, PricingKind.ADULT);
        BigDecimal teenUnit  = applyRules(baseUnit, rules, PricingKind.TEEN);

        // 좌석 수와 인원 수가 다른 경우(프론트에서 좌석만 보내는 시나리오) → 전부 ADULT로 간주
//        BigDecimal totalPrice;
//        if (totalCount == cntAdult + cntTeen) {
//            totalPrice = adultUnit.multiply(BigDecimal.valueOf(cntAdult))
//                    .add(teenUnit.multiply(BigDecimal.valueOf(cntTeen)));
//        } else {
//            totalPrice = adultUnit.multiply(BigDecimal.valueOf(totalCount));
//        }

        // ==== 예약 생성 ====
        Booking booking = new Booking();
        booking.setMember(new Member(memberId));
        booking.setScreening(screening);
        booking.setTotalPrice(total);
        booking.setPaymentStatus(BookingPayStatus.PENDING);
        booking.setBookingTime(now);
        booking = bookingRepository.save(booking);

        for (Seat seat : seats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setScreening(screening);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }

        return new CreateBookingResponse(
                booking.getBookingId(),
                screening.getScreeningId(),
                req.seatIds(),
                total,
                booking.getPaymentStatus().name()
        );
    }

    @Transactional
    public Booking getMyBooking(Long memberId, Long bookingId) {
        // 본인 소유 예매만 조회
        return bookingRepository.findByBookingIdAndMember_MemberId(bookingId, memberId)
                .orElseThrow(() -> new SecurityException("본인 예매만 조회할 수 있습니다."));
    }

    @Transactional
    public void cancel(Long memberId, Long bookingId, @Nullable String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 없습니다."));

        // (선택) 본인 확인
        if (memberId != null && !booking.getMember().getMemberId().equals(memberId)) {
            throw new SecurityException("본인 예매만 취소할 수 있습니다.");
        }

        // 좌석 매핑 삭제 (레포지토리에 메서드가 있어야 합니다)
        bookingSeatRepository.deleteByBookingId(bookingId);

        // 상태 변경
        booking.setPaymentStatus(BookingPayStatus.CANCELLED);
        bookingRepository.save(booking);

        // 취소 로그 남기기(선택)
//        cancelLogRepository.save(CancelLog.ofMemberCancel(booking, memberId, reason));
    }

    @Transactional
    public void releaseHold(Long memberId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return;

        // 소유자 확인 (Booking.member가 null일 수 있으므로 안전하게)
        if (memberId != null) {
            Long ownerId = null;
            if (booking.getMember() != null) {
                // ↓ 프로젝트의 Member 엔티티에 맞춰 한 줄만 선택하세요.
                // ownerId = booking.getMember().getId();
                ownerId = booking.getMember().getMemberId();  // 보통 이렇게 되어 있음
            }
            if (!Objects.equals(ownerId, memberId)) return;
        }

        // 1) 좌석 상태 AVAILABLE로
        seatRepository.releaseSeatsByBookingId(bookingId);

        // 2) seat_hold 삭제
        seatHoldRepository.deleteByBookingId(bookingId);
    }

    @Transactional
    public void releaseHoldByKey(String holdKey) {
        if (holdKey == null || holdKey.isBlank()) return;
        seatHoldRepository.deleteByHoldKey(holdKey);
        // 또는 seatHoldRepository.markReleasedByHoldKey(holdKey);
        // 좌석 상태를 별도 테이블에서 관리한다면 그 부분도 업데이트
    }

    /** pricing_rule 목록을 주어진 kind에 맞게 baseUnit에 적용 */
    private BigDecimal applyRules(BigDecimal baseUnit, List<PricingRule> rules, PricingKind kind) {
        BigDecimal price = baseUnit;

        for (PricingRule r : rules) {
            // kind 매칭 (필요하면 ALL 같은 공통용 kind 도입 가능)
            if (r.getKind() != kind) continue;

            BigDecimal amt = r.getAmount() == null ? BigDecimal.ZERO : r.getAmount();

            PricingOp op = r.getOp();
            if (op == null) continue;

            switch (op) {
                case PLUS -> price = price.add(amt);
                case MINUS -> price = price.subtract(amt).max(BigDecimal.ZERO);
                case PCT_PLUS -> price = price.add(price.multiply(amt).divide(BigDecimal.valueOf(100)));
                case PCT_MINUS -> {
                    BigDecimal dec = price.multiply(amt).divide(BigDecimal.valueOf(100));
                    price = price.subtract(dec).max(BigDecimal.ZERO);
                }
            }
        }
        return price;
    }
}