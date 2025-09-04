package com.gudrhs8304.ticketory.feature.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class OAuthController {

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Value("${kakao.logout-redirect-uri:/}")
    private String kakaoLogoutRedirectUri;

    @Operation(summary = "카카오 로그인 시작", description = "카카오 인증 페이지로 리다이렉트")
    @GetMapping("/api/members/kakao")
    public String kakaoRedirect() {
        return "redirect:/oauth2/authorization/kakao";
    }

//     XHR로 호출하는 경우를 위한 보조 엔드포인트(리액트 수정 불필요)
    @Operation(summary = "카카오 로그인 시작(XHR 보조)", description = "카카오 인증 URL을 반환")
    @PostMapping(value = "/api/members/kakao", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> kakaoStartForAjax() {
        return Map.of("authorizeUrl", "/oauth2/authorization/kakao");
    }

    @GetMapping("/login/success")
    public String loginSuccess() {
        return "redirect:/";
    }



    @Operation(summary = "카카오 로그아웃", description = "카카오 페이지로 리다이렉트 후 프론트로 복귀", security = {})
    @GetMapping({"/kakao/logout", "/api/members/logout/kakao"})
    public void kakaoLogout(HttpServletResponse response) throws IOException {
        String url = "https://kauth.kakao.com/oauth/logout"
                + "?client_id=" + kakaoRestApiKey
                + "&logout_redirect_uri=" + URLEncoder.encode(kakaoLogoutRedirectUri, StandardCharsets.UTF_8);
        response.sendRedirect(url);
    }

    @ResponseBody
    @GetMapping("/login")
    public String loginPage(@RequestParam(required=false) String oauth2_error) {
        return "login page" + (oauth2_error != null ? " (error=" + oauth2_error + ")" : "");
    }

}
