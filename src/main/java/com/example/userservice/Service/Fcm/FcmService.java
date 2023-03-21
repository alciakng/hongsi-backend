package com.example.userservice.Service.Fcm;

import com.example.userservice.Config.Enum.FcmMsgConfig;
import com.example.userservice.Dto.Fcm.PushNotificationRequest;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class FcmService {

    private Logger logger = LoggerFactory.getLogger(FcmService.class);

    public void sendMessage(Map<String, String> data, PushNotificationRequest pushNotificationRequest)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithData(data, pushNotificationRequest);
        String response = sendAndGetResponse(message);
        logger.info("Sent message with data. Topic: " + pushNotificationRequest.getTopic() + ", " + response);
    }

    public void sendMessageWithoutData(PushNotificationRequest pushNotificationRequest)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithoutData(pushNotificationRequest);
        String response = sendAndGetResponse(message);
        logger.info("Sent message without data. Topic: " + pushNotificationRequest.getTopic() + ", " + response);
    }

    public void sendMessageToToken(PushNotificationRequest pushNotificationRequest)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageToToken(pushNotificationRequest);
        String response = sendAndGetResponse(message);
        logger.info("Sent message to token. Device token: " + pushNotificationRequest.getToken() + ", " + response);
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }

    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setSound(FcmMsgConfig.SOUND.getValue())
                        .setColor(FcmMsgConfig.COLOR.getValue()).setTag(topic).build()).build();
    }

    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }

    private Message getPreconfiguredMessageToToken(PushNotificationRequest pushNotificationRequest) {
        return getPreconfiguredMessageBuilder(pushNotificationRequest).setToken(pushNotificationRequest.getToken())
                .build();
    }

    private Message getPreconfiguredMessageWithoutData(PushNotificationRequest pushNotificationRequest) {
        return getPreconfiguredMessageBuilder(pushNotificationRequest).setTopic(pushNotificationRequest.getTopic())
                .build();
    }

    private Message getPreconfiguredMessageWithData(Map<String, String> data, PushNotificationRequest pushNotificationRequest) {
        return getPreconfiguredMessageBuilder(pushNotificationRequest).putAllData(data).setTopic(pushNotificationRequest.getTopic())
                .build();
    }


    private Message.Builder getPreconfiguredMessageBuilder(PushNotificationRequest pushNotificationRequest) {
        // Step1.
        AndroidConfig androidConfig = getAndroidConfig(pushNotificationRequest.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(pushNotificationRequest.getTopic());

        return Message.builder()
                .setApnsConfig(apnsConfig).setAndroidConfig(androidConfig).setNotification(
                        Notification.builder().setTitle(pushNotificationRequest.getTitle()).setBody(pushNotificationRequest.getMessage()).build());
    }


}