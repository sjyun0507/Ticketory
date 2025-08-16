package com.gudrhs8304.ticketory.storage;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@ConditionalOnProperty(name = "app.storage", havingValue = "ftp")
@Component
public class FtpFileStorage implements FileStorage {

    @Value("${ftp.host}") private String host;
    @Value("${ftp.port:21}") private int port;
    @Value("${ftp.username}") private String username;
    @Value("${ftp.password}") private String password;

    @Value("${ftp.base-dir:/html}") private String baseDir;        // 원격 저장 루트
    @Value("${ftp.base-url}") private String baseUrl;              // 공개 URL prefix

    @Override
    public String save(String relPath, String filename, MultipartFile file) {
        FTPClient ftp = new FTPClient();
        try (InputStream in = file.getInputStream()) {
            ftp.connect(host, port);
            if (!ftp.login(username, password)) {
                throw new RuntimeException("FTP 로그인 실패");
            }
            ftp.enterLocalPassiveMode();              // PASV 모드
            ftp.setFileType(FTP.BINARY_FILE_TYPE);    // 바이너리

            // 디렉토리 생성(깊이 생성)
            String fullDir = normalize(baseDir + "/" + relPath);
            ensureDirectories(ftp, fullDir);

            // 업로드
            String remotePath = normalize(fullDir + "/" + filename);
            boolean ok = ftp.storeFile(new String(remotePath.getBytes(StandardCharsets.UTF_8), "ISO-8859-1"), in);
            if (!ok) throw new RuntimeException("FTP 업로드 실패: " + ftp.getReplyString());

            // 공개 URL 반환
            String publicUrl = normalize(baseUrl + "/" + relPath + "/" + filename).replace("//", "/");
            if (publicUrl.startsWith("http:/") || publicUrl.startsWith("https:/")) {
                publicUrl = publicUrl.replace(":/", "://"); // 이중슬래시 보정
            }
            return publicUrl;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        } finally {
            try { if (ftp.isConnected()) { ftp.logout(); ftp.disconnect(); } } catch (Exception ignore) {}
        }
    }

    private void ensureDirectories(FTPClient ftp, String dirPath) throws IOException {
        String[] parts = dirPath.split("/");
        String path = "";
        for (String p : parts) {
            if (p == null || p.isBlank()) continue;
            path += "/" + p;
            ftp.makeDirectory(path);
        }
    }

    private String normalize(String p) {
        return p.replace("\\", "/");
    }
}
