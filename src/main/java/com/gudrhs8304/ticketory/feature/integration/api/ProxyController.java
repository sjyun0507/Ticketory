// src/main/java/com/gudrhs8304/ticketory/controller/ProxyController.java
package com.gudrhs8304.ticketory.feature.integration.api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    private final RestClient http = RestClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "TicketoryImageProxy/1.0")
            .build();

    /**
     * 외부 이미지 프록시
     * 예: /proxy/img?url=http%3A%2F%2Frhsdl2.dothome.co.kr%2Fposters%2Fm2%2F...
     */
    @GetMapping(value = "/img", produces = MediaType.ALL_VALUE)
    public ResponseEntity<byte[]> img(@RequestParam("url") String decodedUrl) {
        // 1) 유효성 검증
        if (decodedUrl == null || !(decodedUrl.startsWith("http://") || decodedUrl.startsWith("https://"))) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("invalid url".getBytes(StandardCharsets.UTF_8));
        }

        try {
            // 2) 업스트림 요청 (리퍼러/쿠키 제거)
            ResponseEntity<byte[]> upstream = http.get()
                    .uri(URI.create(decodedUrl))         // Spring이 이미 디코드된 문자열을 넘겨줌
                    .header(HttpHeaders.REFERER, "")
                    .header(HttpHeaders.COOKIE, "")
                    .retrieve()
                    .toEntity(byte[].class);

            MediaType contentType = upstream.getHeaders().getContentType();
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM;

            HttpHeaders out = new HttpHeaders();
            out.setContentType(contentType);
            out.setCacheControl(CacheControl.maxAge(Duration.ofHours(12)).cachePublic());

            // ✅ HttpStatusCode 사용
            return ResponseEntity.status(upstream.getStatusCode())
                    .headers(out)
                    .body(upstream.getBody());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("proxy error: " + ex.getClass().getSimpleName()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /** 외부 URL만 프록시로 래핑 */
    public static String proxifyExternal(String url) {
        if (url == null || url.isBlank()) return url;
        if (url.startsWith("/proxy/")) return url;      // 이미 프록시
        if (url.startsWith("/")) return url;            // 내부 경로는 그대로
        if (url.startsWith("http://") || url.startsWith("https://")) {
            String enc = URLEncoder.encode(url, StandardCharsets.UTF_8);
            return "/proxy/img?url=" + enc;
        }
        return url;
    }



    /** 동일 의미의 헬퍼 (프로젝트 내에서 이걸 쓰셔도 됩니다) */
    public static String proxify(String url) {
        if (url == null || url.isBlank()) return url;
        if (url.startsWith("/proxy/") || url.startsWith("/")) return url; // 이미 프록시 or 내부 경로

        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                java.net.URI u = java.net.URI.create(url);
                String host = (u.getHost() == null ? "" : u.getHost().toLowerCase());

                // ✅ dothome는 프록시 안 거침 (필요하면 배열로 더 추가)
                if (host.endsWith(".dothome.co.kr")
                        || host.equals("dothome.co.kr")
                        || host.equals("mybusiness.dothome.co.kr")
                        || host.equals("rhsdl2.dothome.co.kr")) {
                    return url;
                }

                // 그 외 외부만 프록시
                return "/proxy/img?url=" + java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignore) {
                return url; // 파싱 실패 시 원본 유지
            }
        }
        return url;
    }
}