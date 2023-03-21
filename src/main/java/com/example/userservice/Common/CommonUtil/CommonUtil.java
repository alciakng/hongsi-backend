package com.example.userservice.Common.CommonUtil;

import com.example.userservice.Security.UserDetail.UserDetailsImpl;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

public class CommonUtil {
    private static final String FILE_EXTENSION_SEPARATOR = ".";
    private static final String CATEGORY_PREFIX ="/";

    private static final String TIME_SEPARATOR = "_";

    public static String buildFileName(String category, String originalFileName) {
        int fileExtensionIndex = originalFileName.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        String fileExtension = originalFileName.substring(fileExtensionIndex);
        String fileName = originalFileName.substring(0, fileExtensionIndex);
        String now = String.valueOf(System.currentTimeMillis());

        return category + CATEGORY_PREFIX + fileName + TIME_SEPARATOR + now + fileExtension;
    }


    public static String getRemoteAddr(){
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = req.getHeader("X-FORWARDED-FOR");
        if (ip == null)
            ip = req.getRemoteAddr();

       return ip;
    }


    public static void setSentryConfig(Exception ex){
        // Authentication 정보
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();

        // authentication이 null 이면 return
        if(authentication ==null){
            return;
        }
        // User 정보 추출
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Sentry identify User - 에러 기록시 User를 식별하기 위함
        User user = new User();
        user.setEmail(userDetails.getEmail());
        user.setUsername(userDetails.getUsername());
        user.setIpAddress(getRemoteAddr());
        Sentry.setUser(user);

        Sentry.captureException(ex);
    }


}
