package com.gudrhs8304.ticketory.feature.booking;

import com.gudrhs8304.ticketory.feature.booking.dto.InitBookingRequestDTO;
import com.gudrhs8304.ticketory.feature.booking.dto.InitBookingResponseDTO;
import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.pricing.PricingRuleRepository;
import com.gudrhs8304.ticketory.feature.pricing.domain.PricingRule;
import com.gudrhs8304.ticketory.feature.seat.Seat;
import com.gudrhs8304.ticketory.feature.seat.SeatHold;
import com.gudrhs8304.ticketory.feature.screening.ScreeningRepository;
import com.gudrhs8304.ticketory.feature.seat.SeatHoldRepository;
import com.gudrhs8304.ticketory.feature.seat.SeatRepository;
import com.gudrhs8304.ticketory.feature.screening.Screening;
import com.gudrhs8304.ticketory.feature.payment.Payment;
import com.gudrhs8304.ticketory.feature.payment.PaymentRepository;
import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.payment.PaymentProvider;
import com.gudrhs8304.ticketory.feature.payment.PaymentStatus;
import com.gudrhs8304.ticketory.feature.point.PricingKind;
import com.gudrhs8304.ticketory.feature.pricing.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookingOrchestrator {

    private final ScreeningRepository screeningRepo;
    private final SeatRepository seatRepo;
    private final SeatHoldRepository seatHoldRepo;
    private final BookingRepository bookingRepo;
    private final BookingSeatRepository bookingSeatRepo;
    private final PaymentRepository paymentRepo;
    private final MemberRepository memberRepo;
    private final PricingRuleRepository pricingRuleRepo;
    private final PricingService pricingService;

    private static final int DEFAULT_HOLD_SECONDS = 120;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int WED_DISCOUNT_PERCENT = 20; // 20%

    @Transactional
    public InitBookingResponseDTO initBooking(Long memberId, String idemKey, InitBookingRequestDTO req) {
        long t0 = System.currentTimeMillis();
        MDC.put("memberId", String.valueOf(memberId));
        if (req.screeningId() == null || req.seatIds() == null || req.seatIds().isEmpty()) {
            throw new IllegalArgumentException("screeningId/seatIds는 필수입니다.");
        }

        int adults = Optional.ofNullable(req.counts()).map(m -> m.getOrDefault("adult", 0)).orElse(0);
        int teens  = Optional.ofNullable(req.counts()).map(m -> m.getOrDefault("teen", 0)).orElse(0);
        if (req.seatIds().size() != adults + teens) {
            throw new IllegalArgumentException("선택 좌석 수와 인원 수가 일치하지 않습니다.");
        }

        int holdSec = DEFAULT_HOLD_SECONDS;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(holdSec);

        // 1) 상영 로드
        Screening screening = screeningRepo.findById(req.screeningId())
                .orElseThrow(() -> new IllegalArgumentException("상영 정보 없음"));
        Long screenId = screening.getScreen().getScreenId();

        // 2) 좌석 소속 검증 + 잠금
        boolean ok = seatRepo.allSeatsBelongToScreen(req.seatIds(), screenId);
        if (!ok) throw new IllegalArgumentException("상영관 외 좌석 포함");
        List<Seat> seats = seatRepo.lockSeatsForUpdate(req.seatIds());

        // 3) 중복 점유 검사(확정/홀드)
        Set<Long> bookedSeatIds = bookingSeatRepo.findSeatIdsByScreeningPaidOrExists(req.screeningId());
        if (seats.stream().anyMatch(s -> bookedSeatIds.contains(s.getSeatId())))
            throw new IllegalStateException("이미 예매 완료된 좌석 포함");

        boolean hasActiveHold = seatHoldRepo.existsAnyActiveHold(req.screeningId(), req.seatIds(), now);
        if (hasActiveHold) throw new IllegalStateException("이미 홀드된 좌석 포함");

        // 4) HOLD 생성
        List<Long> holdIds = new ArrayList<>();
        for (Seat seat : seats) {
            SeatHold h = SeatHold.builder()
                    .screening(screening)
                    .seat(seat)
                    .holdTime(holdSec)
                    .expiresAt(expiresAt)
                    .holdKey(idemKey)
                    .build();
            seatHoldRepo.save(h);
            holdIds.add(h.getHoldId());
        }

        // 5) 가격 계산
        BigDecimal base = Optional.ofNullable(screening.getScreen().getBasePrice())
                .map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        LocalDateTime whenForPricing = screening.getStartAt();
        log.info("[CHECK] startAt={} dayOfweek={} hour={}", whenForPricing, whenForPricing.getDayOfWeek(), whenForPricing.getHour());
        BigDecimal unitAdult = calcUnitPrice(base, screenId, PricingKind.ADULT, whenForPricing);
        BigDecimal unitTeen  = calcUnitPrice(base, screenId, PricingKind.TEEN,  whenForPricing);

        BigDecimal total = unitAdult.multiply(BigDecimal.valueOf(adults))
                .add(unitTeen.multiply(BigDecimal.valueOf(teens)));



        // 5-1) 포인트 사용량 확정 (요청값 → 보유/총액 한도 내로 클램프)
        Member me = memberRepo.getReferenceById(memberId);
        int wantUse = Optional.ofNullable(req.pointsUsed()).orElse(0);
        int have    = Optional.ofNullable(me.getPointBalance()).orElse(0);
        if (wantUse < 0) wantUse = 0;
        if (wantUse > have) wantUse = have;                    // 보유 초과 방지
        if (BigDecimal.valueOf(wantUse).compareTo(total) > 0)  // 총액 초과 방지
            wantUse = total.intValue();

        // 실결제금액 = 총액 - 사용포인트
        BigDecimal payable = total.subtract(BigDecimal.valueOf(wantUse));
        if (payable.compareTo(BigDecimal.ZERO) < 0) payable = BigDecimal.ZERO;

        // 6) BOOKING (총액 저장)
        Booking booking = Booking.builder()
                .member(me)
                .screening(screening)
                .bookingTime(now)
                .totalPrice(total)
                .paymentStatus(BookingPayStatus.PENDING)
                .build();
        bookingRepo.save(booking);

        // 7) PAYMENT(PENDING) — amount = 실결제금액
        String orderId = newOrderId(booking.getBookingId(), idemKey);
        String paymentKey = (idemKey == null || idemKey.isBlank())
                ? "IDEMP-" + UUID.randomUUID()
                : idemKey;

        PaymentProvider provider = PaymentProvider.TOSS;
        if (req.provider() != null) {
            try { provider = PaymentProvider.valueOf(req.provider()); } catch (Exception ignore) {}
        }

        Payment pay = Payment.builder()
                .booking(booking)
                .orderId(orderId)
                .paymentKey(paymentKey)
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .amount(payable) // ★ 포인트 차감 반영된 금액
                .build();
        paymentRepo.save(pay);

        // 8) 응답 (프론트 결제창에서 바로 사용)
        return new InitBookingResponseDTO(
                booking.getBookingId(),
                pay.getPaymentId(),
                holdIds,
                expiresAt.toString(),
                pay.getStatus().name(),
                pay.getProvider().name(),
                total,      // 총액
                wantUse,    // 사용 포인트
                payable     // 실결제 금액
        );
    }

    /** 주문번호 생성: 예) ORD-20250825-<bookingId>-AB12CD34 */
    private String newOrderId(Long bookingId, String idemKey) {
        String ymd = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + ymd + "-" + bookingId + "-" + shortUuid;
    }

    @Transactional
    public void releaseHold(Long memberId, Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking == null) return;
        Long ownerId = (booking.getMember() != null) ? booking.getMember().getMemberId() : null;
        if (memberId != null && !Objects.equals(ownerId, memberId)) return;

        seatRepo.releaseSeatsByBookingId(bookingId);
        seatHoldRepo.deleteByBookingId(bookingId);
    }

    /** 단가 계산: basePrice + pricing_rule(op/amount/priority) */
    private BigDecimal calcUnitPrice(BigDecimal basePrice,
                                     Long screenId,
                                     PricingKind kind,
                                     LocalDateTime when) {
        BigDecimal price = (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0)
                ? basePrice : new BigDecimal("12000");

        List<PricingRule> rules = pricingRuleRepo.findActiveRulesByKind(screenId, kind, when);
        for (PricingRule r : rules) {
            BigDecimal amt = (r.getAmount() != null) ? r.getAmount() : BigDecimal.ZERO;
            switch (r.getOp()) {
                case SET      -> price = amt;
                case PLUS     -> price = price.add(amt);
                case MINUS    -> price = price.subtract(amt);
                case PCT_PLUS -> price = price.multiply(BigDecimal.ONE.add(amt.movePointLeft(2)));
                case PCT_MINUS-> price = price.multiply(BigDecimal.ONE.subtract(amt.movePointLeft(2)));
                default -> { }
            }
        }

        DayOfWeek dow = when.atZone(ZoneId.systemDefault()).withZoneSameInstant(KST).getDayOfWeek();
        if (dow == DayOfWeek.WEDNESDAY) {
            BigDecimal pct = BigDecimal.valueOf(WED_DISCOUNT_PERCENT).movePointLeft(2); // 0.20
            price = price.multiply(BigDecimal.ONE.subtract(pct));
        }

        return price.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP);
    }
}