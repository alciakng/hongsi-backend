package com.example.userservice.Service.Common;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.userservice.Common.CommonUtil.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommonService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    /*
     * URL에서 이미지를 다운로드하여 MultipartFile 객체로 변환
     */
    public MultipartFile downloadImageAsMultipartFile(String imageUrl) throws IOException {
        // Step1. Create a temporary file to store the downloaded image (Path 객체이용)
        Path tempFile = Files.createTempFile("image", ".jpg");

        // Step2. URL에서 이미지 다운로드하여 tempFile(Path객체) 로 COPY
        try (InputStream in = new URL(imageUrl).openStream()) {
            // Download the image data from the URL and write it to the temporary file
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e){
            throw e;
        }

        // Step3. Convert the temporary file to a MultipartFile object and return it
        return new MockMultipartFile("image", tempFile.getFileName().toString(),
                Files.probeContentType(tempFile), Files.newInputStream(tempFile));
    }


    public String uploadFile(String category, MultipartFile multipartFile) throws IOException {

        // Step1. 파일 유효성 체크
        validateFileExists(multipartFile);

        // Step2. 파일 이름 생성규칙
        String fileName = CommonUtil.buildFileName(category, multipartFile.getOriginalFilename());

        // Step3. 파일의 사이즈를 ContentLength로 S3에 알려주기 위해서 ObjectMetadata를 사용합니다.
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        // Step4. S3 API 메소드인 putObject를 이용하여 파일 Stream을 열어서 S3에 파일을 업로드 합니다.
        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) { // TODO 에러처리 고민할 것
            throw e;
        }

        // Step4. getUrl 메소드를 통해서 S3에 업로드된 사진 URL을 가져오는 방식
        return amazonS3.getUrl(bucketName, fileName).getPath();
    }

    private void validateFileExists(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new RuntimeException("파일크기가 0 입니다. 네트워크 상태를 확인하세요.");
        }
    }

}