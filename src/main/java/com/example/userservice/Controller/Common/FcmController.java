package com.example.userservice.Controller.Common;

import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Dto.Fcm.PushNotificationRequest;
import com.example.userservice.Service.Common.PushNotificationService;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final PushNotificationService pushNotificationService;
    @Value("${fcm.key.scope}")
    private String scope;
    @Value("${fcm.key.path}")
    private String firebaseConfigPath;
    @PostMapping("/accessToken")
    public String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(scope);
        googleCredentials.refreshAccessToken();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    @PostMapping("/topic")
    public ResponseEntity<Object> sendNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationWithoutData(request);
        return ApiResponse.ApiSuccess("Message has been sent.");
    }

    @PostMapping("/token")
    public ResponseEntity<Object> sendTokenNotification(@RequestBody PushNotificationRequest request) throws ExecutionException, InterruptedException {
        pushNotificationService.sendPushNotificationToToken(request);
        return ApiResponse.ApiSuccess("Message has been sent.");
    }


    /*
    @PostMapping("/data")
    public ResponseEntity sendDataNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }

    @GetMapping("/noti")
    public ResponseEntity sendSampleNotification() {
        fcmService.sendSamplePushNotification();
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }
    */



}
