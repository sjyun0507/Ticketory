package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.dto.JwtResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberLoginRequestDTO;
import com.gudrhs8304.ticketory.dto.MemberResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberSignupRequestDTO;

public interface MemberService {
    MemberResponseDTO signUp(MemberSignupRequestDTO req);

    JwtResponseDTO login(MemberLoginRequestDTO req);
}
