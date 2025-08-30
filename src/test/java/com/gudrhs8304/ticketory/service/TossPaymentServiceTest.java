package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.screening.domain.Screening;
import com.gudrhs8304.ticketory.feature.payment.Payment;
import com.gudrhs8304.ticketory.feature.payment.PaymentStatus;
import com.gudrhs8304.ticketory.feature.payment.dto.TossConfirmRequestDTO;
import com.gudrhs8304.ticketory.feature.payment.dto.TossInitiateRequestDTO;
import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.payment.TossPaymentService;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.screening.ScreeningRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(TossPaymentServiceTest.TestTossClientConfig.class) // (2) 가짜 WebClient 주입
class TossPaymentServiceTest {

    @Autowired
    TossPaymentService tossPaymentService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ScreeningRepository screeningRepository;
    @Autowired
    MemberRepository memberRepository;

    @Test
    void testConfirmPayment() {
        // --- 준비: Member / Screening 확보 ---
        Member member = memberRepository.findByEmail("admin@ticketory.com")
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .email("tester@ticketory.com")
                                .name("테스터")
                                .loginId("tester")
                                .build()
                ));

        Screening screening = screeningRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 Screening이 필요합니다."));

        // --- given: Booking 저장 (screening NOT NULL!) ---
        Booking booking = bookingRepository.save(
                Booking.builder()
                        .member(member)
                        .screening(screening)
                        .totalPrice(BigDecimal.valueOf(14000))
                        .build()
        );

        // --- 결제 시작 파라미터 생성 ---
        TossInitiateRequestDTO initReq =
                new TossInitiateRequestDTO(booking.getBookingId(), BigDecimal.valueOf(14000));
        var initiateRes = tossPaymentService.initiate(initReq, member.getMemberId());

        // --- when: 결제 승인 (가짜 WebClient가 200/빈 JSON 반환) ---
        TossConfirmRequestDTO confirmReq = new TossConfirmRequestDTO(
                "fake_key_123", initiateRes.orderId(), BigDecimal.valueOf(14000)
        );
        Payment payment = tossPaymentService.confirm(confirmReq);

        // --- then ---
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaymentKey()).isEqualTo("fake_key_123");
        assertThat(payment.getOrderId()).isEqualTo(initiateRes.orderId());
    }

    /**
     * (2) 실제 HTTP 호출 없이 WebClient 응답을 가짜로 만들어주는 테스트 전용 설정
     */
    @TestConfiguration
    static class TestTossClientConfig {
        @Bean
        @Primary
        WebClient tossClient() {
            // 항상 200 OK + 빈 JSON 응답을 돌려주는 ExchangeFunction
            return WebClient.builder()
                    .baseUrl("http://stub.toss.local")
                    .exchangeFunction(request ->
                            reactor.core.publisher.Mono.just(
                                    ClientResponse
                                            .create(org.springframework.http.HttpStatus.OK)
                                            .header("Content-Type", "application/json")
                                            .body("{}")
                                            .build()
                            )
                    )
                    .build();
        }
    }
}