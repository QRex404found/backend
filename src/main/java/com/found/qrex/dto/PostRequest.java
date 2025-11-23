package com.found.qrex.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostRequest {
    private String title;
    private String content;
    private String url; // ⭐ URL 필드 추가
    private String writerId;
}
