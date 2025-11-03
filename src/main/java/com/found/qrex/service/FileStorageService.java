package com.found.qrex.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class FileStorageService {

    /**
     * 파일을 (가상으로) 업로드하고, 접근 가능한 URL을 반환합니다.
     * 실제 S3나 서버에 저장하는 로직 대신, 임시 플레이스홀더 이미지 URL을 반환합니다.
     * * @param file 업로드된 파일
     * @return DB에 저장될 이미지 URL (파일이 없으면 null)
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null; // 업로드된 파일이 없음
        }

        // 실제 파일 이름 (디버깅용)
        String originalFilename = file.getOriginalFilename();
        System.out.println("Mock File Upload: Received file: " + originalFilename);

        // (중요) 실제 파일 대신, 이미지가 업로드된 것처럼 보이는
        // '플레이스홀더(Placeholder)' 이미지의 공용 URL을 반환합니다.
        // 이 URL이 DB의 imagePath에 저장됩니다.
        return "https://placehold.co/600x400/EEE/31343C?text=Uploaded_Image";
    }
}
