package com.example.userservice.Controller.Common;

import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Service.Common.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(
            @RequestParam("category") String category,
            @RequestPart(value = "file") MultipartFile multipartFile) throws IOException {
        String url = commonService.uploadFile(category, multipartFile);
        return ApiResponse.ApiSuccess(url);
    }

    @GetMapping("/imageDownload")
    public MultipartFile downloadImageAsMultipartFile(@RequestParam("imageUrl") String imageUrl) throws IOException {
        return commonService.downloadImageAsMultipartFile(imageUrl);
    }

}
