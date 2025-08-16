package com.gudrhs8304.ticketory.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConditionalOnProperty(name = "app.storage", havingValue = "local", matchIfMissing = true)
@Component
public class LocalFileStorage implements FileStorage {

    @Value("${app.upload.dir:uploads}") // application.properties에서 경로 지정 가능
    private String uploadDir;

    @Value("${app.upload.base-url:/files}")
    private String baseUrl;

    @Override
    public String save(String relPath, String filename, MultipartFile file) {
        try {
            // 절대경로 기준으로 정규화
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();

            // path traversal 방지: 외부에서 넘어온 경로를 정규화 후 base 하위인지 확인
            Path target = base.resolve(relPath).resolve(filename).normalize();
            if(!target.startsWith(base)) {
                throw new SecurityException("잘못된 경로입니다.");
            }

            // 상위 디렉토리 생성
            Files.createDirectories(target.getParent());

            // 저장
            file.transferTo(target.toFile());

            // 공개 URL 생성 (역슬래시 -> 슬래시, 이중 슬래시 제거)
            String urlPath = baseUrl + "/" + base.relativize(target).toString().replace("\\", "/");
            urlPath = urlPath.replaceAll("//+", "/");
            return urlPath;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
