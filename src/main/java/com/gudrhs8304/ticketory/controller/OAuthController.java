package com.gudrhs8304.ticketory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class OAuthController {

    @Operation(summary = "카카오 로그인 시작", description = "카카오 인증 페이지로 리다이렉트")
    @GetMapping("/api/members/kakao")
    public String kakaoRedirect() {
        return "redirect:/oauth2/authorization/kakao";
    }

    @ResponseBody
    @GetMapping("/login/success")
    public String loginSuccess() {
        return "kakao login ok";
    }

    @ResponseBody
    @GetMapping("/login")
    public String loginPage(@RequestParam(required=false) String oauth2_error) {
        return "login page" + (oauth2_error != null ? " (error=" + oauth2_error + ")" : "");
    }

}
