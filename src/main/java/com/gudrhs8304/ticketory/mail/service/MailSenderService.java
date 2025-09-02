package com.gudrhs8304.ticketory.mail.service;

import com.gudrhs8304.ticketory.feature.booking.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.booking.repository.BookingRepository;
import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.mail.domain.MailLog;
import com.gudrhs8304.ticketory.mail.repository.MailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class MailSenderService {

    private final JavaMailSender mailSender;
    private final MailLogRepository mailLogRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${myapp.custom.mail.sender.mailFrom}")
    private String mailFrom;

    @Value("${myapp.custom.mail.sender.mailFromName}")
    private String mailFromName;

    // ----- 유틸: 이미지 받아오기(컨텐츠 타입 감지) -----
    private static class Img {
        final byte[] bytes;
        final String contentType;

        Img(byte[] b, String ct) {
            this.bytes = b;
            this.contentType = ct;
        }
    }

    private Img fetchImage(String url) {
        try {
            if (url == null || url.isBlank()) return null;

            // data URL (예: data:image/png;base64,....)
            if (url.startsWith("data:image/")) {
                int comma = url.indexOf(',');
                if (comma > 0) {
                    String meta = url.substring(5, comma); // "image/png;base64"
                    String base64 = url.substring(comma + 1);
                    String contentType = meta.split(";")[0]; // image/png
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    return new Img(bytes, contentType);
                }
            }

            var resp = restTemplate.getForEntity(url, byte[].class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("이미지 응답 비정상 status={}, url={}", resp.getStatusCode(), url);
                return null;
            }
            String ct = null;
            var ctHdr = resp.getHeaders().getContentType();
            if (ctHdr != null) ct = ctHdr.toString();
            if (ct == null || !ct.startsWith("image/")) {
                // 그래도 붙일 순 있음(일부 서버가 text/plain 으로 줌) → jpg로 가정
                ct = "image/jpeg";
            }
            return new Img(resp.getBody(), ct);
        } catch (Exception e) {
            log.warn("이미지 로드 실패 url={}, err={}", url, e.toString());
            return null;
        }
    }

    // ----- 유틸: QR 직접 생성 (ZXing) -----
    private Img generateQrPng(String payload, int size) {
        try {
            var writer = new com.google.zxing.qrcode.QRCodeWriter();
            var hints = new java.util.EnumMap<com.google.zxing.EncodeHintType, Object>(com.google.zxing.EncodeHintType.class);
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
            var matrix = writer.encode(payload, com.google.zxing.BarcodeFormat.QR_CODE, size, size, hints);
            var image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            try (var baos = new java.io.ByteArrayOutputStream()) {
                javax.imageio.ImageIO.write(image, "png", baos);
                return new Img(baos.toByteArray(), "image/png");
            }
        } catch (Exception e) {
            log.warn("QR 생성 실패 payload={}, err={}", payload, e.toString());
            return null;
        }
    }

    public void sendMailForAlarm(String to,
                                 String movieTitle,
                                 String startAt,
                                 Long bookingId,
                                 String qrUrl,
                                 String posterUrl)
            throws MessagingException, UnsupportedEncodingException {

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            log.warn("[MAIL][SKIP] booking not found. bookingId={}", bookingId);
            return; // 또는 필요하면 MailLog 에 FAIL 로 남겨도 됨
        }
        if (booking.getPaymentStatus() != BookingPayStatus.PAID) {
            log.info("[MAIL][SKIP] payment_status is not PAID. bookingId={}, status={}",
                    bookingId, booking.getPaymentStatus());
            return; // 메일 발송하지 않음
        }

        List<Object[]> seatRows = bookingRepository.findSeatLabelsByBookingIds(List.of(bookingId));
        List<String> seatLabels = seatRows.stream()
                .map(arr -> (String) arr[1]) // arr[1]이 좌석 라벨 ex) "A10"
                .toList();
        String seatsText = seatLabels.isEmpty() ? "-" : escapeHtml(String.join(", ", seatLabels));
        String seatsHtml = seatLabels.isEmpty()
                ? ""
                : "<div style='margin-top:6px;font-size:13px;opacity:.95'>좌석: " + seatsText + "</div>";

        String subject = "[" + movieTitle + "] 상영회차 알림 메일입니다.";

        // ——— 티켓 스타일 HTML (이미지 CID: qr, poster) ———
        String messageText = """
<table role='presentation' cellpadding='0' cellspacing='0' border='0' width='100%' style='background:#f6f7f9; padding:20px 0; font-family:system-ui,-apple-system,Segoe UI,Roboto,Apple SD Gothic Neo,Noto Sans KR,sans-serif'>
  <tr>
    <td align='center'>
      <table role='presentation' cellpadding='0' cellspacing='0' border='0' width='720' style='max-width:720px; background:#ffffff; border-radius:16px; box-shadow:0 10px 30px rgba(0,0,0,.12); overflow:hidden'>
        <tr>
          <!-- LEFT: Poster -->
          <td width='44%' valign='top' style='padding:0;'>
            <img src='cid:poster' width='100%' style='display:block; width:100%; height:auto; border:0; outline:none; text-decoration:none;' alt='포스터'>
          </td>

          <!-- RIGHT: Ticket panel -->
          <td width='56%' valign='top' style='padding:22px;'>
            <!-- chip -->
            <table role='presentation' cellpadding='0' cellspacing='0' border='0' style='margin-bottom:10px'>
              <tr>
                <td style='font-size:12px; color:#111; padding:6px 10px; border:1px solid #e8e8ec; border-radius:999px; background:#fafafa;'>
                  예매 알림 · <span style='font-family:monospace'>#%BOOKING_ID%</span>
                </td>
              </tr>
            </table>

            <div style='font-size:24px; font-weight:800; color:#111; line-height:1.25; margin:4px 0 6px'>
              %TITLE%
            </div>

            <div style='font-size:14px; color:#555; margin:0 0 2px'>Ticketory 대구</div>
            <div style='font-size:14px; color:#333; margin:0 0 10px'><strong>상영 시작</strong> · %START_AT%</div>
            %SEATS%

            <!-- QR -->
            <table role='presentation' cellpadding='0' cellspacing='0' border='0' style='margin:16px 0 0'>
              <tr>
                <td style='font-size:12px; color:#777; padding-bottom:6px'>QR 코드</td>
              </tr>
              <tr>
                <td style='border:1px solid #eee; border-radius:12px; padding:8px;'>
                  <img src='cid:qr' width='160' height='160' alt='QR' style='display:block; width:160px; height:160px;'>
                </td>
              </tr>
            </table>

            <div style='font-size:14px; color:#333; line-height:1.6; margin-top:16px'>
              예매하신 <strong>%TITLE%</strong>의 상영 시간이 다가왔습니다. 즐거운 관람 되세요!
            </div>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
""";
        messageText = messageText
                .replace("%BOOKING_ID%", String.valueOf(bookingId))
                .replace("%TITLE%", escapeHtml(movieTitle))
                .replace("%START_AT%", escapeHtml(startAt))
                .replace("%SEATS%", seatsHtml)        // 좌석 라벨(좌측 큰 영역)
                .replace("%SEATS_TEXT%", seatsText);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // multipart=true
        helper.setTo(to);
        helper.setFrom(new InternetAddress(mailFrom, mailFromName));
        helper.setSubject(subject);
        helper.setText(messageText, true);

        // ----- QR 인라인 (우선: 주어진 qrUrl을 시도 → 실패 시 자체 생성) -----
        Img qrImg = fetchImage(qrUrl);
        if (qrImg == null) {
            // 메일에서는 외부 접근 문제 빈번 → bookingId 기반 QR 생성(혹은 예매확인 URL)
            String fallbackPayload = "TICKETORY-BOOKING:" + bookingId;
            qrImg = generateQrPng(fallbackPayload, 320);
        }
        if (qrImg != null) {
            helper.addInline("qr", new ByteArrayResource(qrImg.bytes), qrImg.contentType);
        } else {
            log.warn("QR 이미지를 첨부하지 못했습니다. (qrUrl={}, bookingId={})", qrUrl, bookingId);
        }

        // ----- 포스터 인라인 -----
        Img posterImg = fetchImage(posterUrl);
        if (posterImg != null) {
            helper.addInline("poster", new ByteArrayResource(posterImg.bytes), posterImg.contentType);
        } else {
            log.warn("포스터 이미지를 첨부하지 못했습니다. posterUrl={}", posterUrl);
        }

        // ----- 발송 + 로그 -----
        try {
            mailSender.send(message);
            log.info("메일 발송 완료 → to={}, subject={}", to, subject);

            mailLogRepository.save(MailLog.builder()
                    .bookingId(bookingId)
                    .recipientEmail(to)
                    .subject(subject)
                    .messageText(messageText)
                    .status(MailLog.Status.SUCCESS)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("메일 발송 실패 → to={}, subject={}, err={}", to, subject, e.toString());
            mailLogRepository.save(MailLog.builder()
                    .bookingId(bookingId)
                    .recipientEmail(to)
                    .subject(subject)
                    .messageText(messageText)
                    .status(MailLog.Status.FAIL)
                    .errorMessage(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build());
            throw e;
        }
    }

    // 간단 HTML escape (제목 등에 따옴표 등 들어올 때 안전)
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}