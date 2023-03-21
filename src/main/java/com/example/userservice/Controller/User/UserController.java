package com.example.userservice.Controller.User;


import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Dto.User.UserDataDTO;
import com.example.userservice.Dto.User.UserResponseDTO;
import com.example.userservice.Model.AppUser;
import com.example.userservice.Service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;


    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody UserDataDTO user) {

        UserResponseDTO userResponseDTO = userService.signUp(modelMapper.map(user, AppUser.class))
                                     .orElseThrow(() -> new RuntimeException("이미 존재하는 유저입니다."));

        return ApiResponse.ApiSuccess(userResponseDTO);
    }

    @DeleteMapping(value = "/{email}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String delete(@PathVariable String email) {
        userService.delete(email);
        return email;
    }

    @GetMapping(value = "/profile")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public UserResponseDTO getProfile(){
        return userService.getProfile().orElseThrow(() -> new RuntimeException("해당 유저정보가 존재하지 않습니다. 네트워크 상태를 확인하세요."));
    }

    @PostMapping(value = "/profile")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public UserResponseDTO saveProfile(UserDataDTO userDataDTO){
        return userService.saveProfile(userDataDTO);
    }



    @GetMapping("/reIssueToken")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public ResponseEntity<Object> reIssueToken() {
        return userService.reIssueToken();
    }


}