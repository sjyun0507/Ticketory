package com.gudrhs8304.ticketory.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    /** 파일을 저장하고 접근 가능한 URL을 반환 */
    String save(String relPath, String filename, MultipartFile file);
}
