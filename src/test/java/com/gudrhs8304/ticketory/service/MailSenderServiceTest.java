package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.mail.MailLogRepository;
import com.gudrhs8304.ticketory.mail.MailSenderService;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
class MailSenderServiceTest {
    @Autowired
    private MailSenderService mailSenderService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private MailLogRepository mailLogRepository;

//    @Test
//    public void sendMailTest() throws
//            Exception {
//
//        mailSenderService.sendMailForAlarm("gudrhs8304@naver.com", "test movie", "2022-08-25",100L);
//    }
//
//    @Test
//    void sendMailAndLog_ShouldSaveLog() throws MessagingException, UnsupportedEncodingException {
//        // given
//        String to = "allenis.dev@gmail.com"; // 테스트용 수신자
//        String movieTitle = "테스트 영화";
//        String startAt = "2025-08-30 18:00";
//
//        // when
//        try {
//            mailSenderService.sendMailForAlarm(to, movieTitle, startAt, 100L);
//        } catch (Exception e) {
//            log.warn("메일 발송 자체는 실패했을 수 있으나 로그는 남아야 함: {}", e.getMessage());
//        }
//
//        // then
//        List<MailLog> logs = mailLogRepository.findAll();
//        assertThat(logs).isNotEmpty();
//
//        MailLog lastLog = logs.get(logs.size() - 1);
//        log.info("저장된 로그: {}", lastLog);
//
//        assertThat(lastLog.getRecipientEmail()).isEqualTo(to);
//        assertThat(lastLog.getSubject()).contains(movieTitle);
//        assertThat(lastLog.getBookingId()).isEqualTo(100L);
//        assertThat(lastLog.getStatus()).isIn(MailLog.Status.SUCCESS, MailLog.Status.FAIL);
//    }
}
