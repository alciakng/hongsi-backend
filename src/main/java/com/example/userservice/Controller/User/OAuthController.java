package com.example.userservice.Controller.User;


import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Dto.OAuth.OAuthRequestDTO;
import com.example.userservice.Dto.User.TokenDTO;
import com.example.userservice.Service.User.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthService oAuthService;

    @PostMapping("/login")
    public ResponseEntity<Object> oAuthLogin(@RequestBody OAuthRequestDTO oAuthRequestDTO) throws IOException {
        TokenDTO tokenDTO = oAuthService.oAuthLogin(oAuthRequestDTO);
        return ApiResponse.ApiSuccess(tokenDTO);
    }

}
