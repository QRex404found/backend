package com.found.qrex.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    // 1. application.properties에서 cloudinary.url 값을 주입받습니다.
    public FileStorageService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        // 주입받은 URL로 Cloudinary 객체를 초기화합니다.
        this.cloudinary = new Cloudinary(cloudinaryUrl);
        // (선택) 보안 URL(https)을 기본으로 사용하도록 설정할 수 있습니다.
        this.cloudinary.config.secure = true;
    }

    /**
     * 파일을 Cloudinary에 업로드하고, 접근 가능한 URL을 반환합니다.
     * @param file 업로드된 파일
     * @return DB에 저장될 이미지 URL (파일이 없으면 null)
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null; // 업로드된 파일이 없음
        }

        try {
            // 2. 파일을 byte[]로 변환하여 Cloudinary에 업로드합니다.
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            // (선택) 파일명이 겹치지 않도록 고유 ID를 지정할 수 있습니다.
                            "public_id", UUID.randomUUID().toString()
                    ));

            // 3. 업로드 성공 시, Cloudinary가 반환한 URL(secure_url)을 반환합니다.
            // 이 URL이 DB의 'imagePath' 컬럼에 저장됩니다.
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file to Cloudinary.", e);
        }
    }
}
