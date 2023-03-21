package com.example.userservice.Dto.Common;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.runner.FilterFactory;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class FileDataDto {
    private FileCategory category;
    private MultipartFile file;

}