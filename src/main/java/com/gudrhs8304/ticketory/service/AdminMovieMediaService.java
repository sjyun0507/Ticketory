package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.Movie;
import com.gudrhs8304.ticketory.domain.MovieMedia;
import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import com.gudrhs8304.ticketory.repository.MovieMediaRepository;
import com.gudrhs8304.ticketory.repository.MovieRepository;
import com.gudrhs8304.ticketory.storage.FileStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMovieMediaService {

    private final MovieRepository movieRepository;
    private final MovieMediaRepository mediaRepository;
    private final FileStorage storage;

    @Value("${app.media.posters-dir:posters}")   private String postersDir;
    @Value("${app.media.stillcuts-dir:stillcuts}") private String stillcutsDir;
    @Value("${app.media.trailers-dir:trailers}") private String trailersDir;



    public MovieMedia uploadImage(Long movieId, MovieMediaType type, MultipartFile file, String description) {
        if (type == MovieMediaType.TRAILER) {
            throw new IllegalArgumentException("트레일러는 /trailer API로 URL을 등록하세요.");
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화 없음: id=" + movieId));

        // 타입별 디렉토리 결정
        String base = switch (type) {
            case POSTER -> postersDir;
            case STILL  -> stillcutsDir;
            default     -> "others";
        };

        // (선택) 영화별 하위 폴더를 두고 싶으면 아래처럼:
        String relPath = base + "/m" + movieId;

        String ext = getExt(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);

        // 저장(FTP 또는 로컬) → 공개 URL 반환
        String url = storage.save(relPath, filename, file);

        MovieMedia media = new MovieMedia();
        media.setMovie(movie);
        media.setMovieMediaType(type);
        media.setUrl(url);
        media.setDescription(description);

        return mediaRepository.save(media);
    }

    // 트레일러 URL 등록 (파일 업로드 없음)
    @Transactional
    public MovieMedia addTrailerUrl(Long movieId, String trailerUrl, String description) {
        var movie = movieRepository.findByMovieIdAndDeletedAtIsNull(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화가 없습니다: " + movieId));

        if (!(trailerUrl.startsWith("http://") || trailerUrl.startsWith("https://"))) {
            throw new IllegalArgumentException("잘못된 URL 형식입니다.");
        }

        MovieMedia media = new MovieMedia();
        media.setMovie(movie);
        media.setMovieMediaType(MovieMediaType.TRAILER);
        media.setUrl(trailerUrl.trim());
        media.setDescription(description);
        return mediaRepository.save(media);
    }

    public List<MovieMedia> list(Long movieId, MovieMediaType type) {
        if (type == null) return mediaRepository.findByMovie_MovieId(movieId);
        return mediaRepository.findByMovie_MovieIdAndMovieMediaType(movieId, type);
    }

    public void delete(Long mediaId) {
        mediaRepository.deleteById(mediaId);
        // (선택) FTP 원본 파일도 지우려면 URL → 경로로 역매핑해서 삭제 로직 추가
    }

    private String getExt(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return (i > -1) ? name.substring(i + 1) : "";
    }
}