package com.gudrhs8304.ticketory.feature.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    /** 파일을 저장하고 접근 가능한 URL을 반환 */
    String save(String relPath, String filename, MultipartFile file);

    /** 바이트 배열 저장 (QR 등) */
    String saveBytes(String relPath, String filename, byte[] data);

    /** 공개 URL로부터 원본 바이트 다운로드 */
    byte[] downloadAsBytes(String publicUrl);
}
