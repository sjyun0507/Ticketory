package com.gudrhs8304.ticketory.feature.movie.service;

import com.gudrhs8304.ticketory.feature.movie.domain.Movie;
import com.gudrhs8304.ticketory.feature.movie.domain.MovieMedia;
import com.gudrhs8304.ticketory.feature.movie.enums.MovieMediaType;
import com.gudrhs8304.ticketory.feature.movie.repository.MovieMediaRepository;
import com.gudrhs8304.ticketory.feature.movie.repository.MovieRepository;
import com.gudrhs8304.ticketory.feature.storage.FileStorage;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final MovieRepository movieRepo;
    private final MovieMediaRepository movieMediaRepo;

    @Value("${app.media.posters-dir:posters}")
    private String postersDir;
    @Value("${app.media.stillcuts-dir:stillcuts}")
    private String stillcutsDir;
    @Value("${app.media.trailers-dir:trailers}")
    private String trailersDir;


    public MovieMedia uploadImage(Long movieId, MovieMediaType type, MultipartFile file, String description) {
        if (type == MovieMediaType.TRAILER) {
            throw new IllegalArgumentException("트레일러는 /trailer API로 URL을 등록하세요.");
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화 없음: id=" + movieId));

        // 타입별 디렉토리 결정
        String base = switch (type) {
            case POSTER -> postersDir;
            case STILL -> stillcutsDir;
            default -> "others";
        };

        // (선택) 영화별 하위 폴더를 두고 싶으면 아래처럼:
        String relPath = base + "/m" + movieId;

        String ext = getExt(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);

        // 저장(FTP 또는 로컬) → 공개 URL 반환
        String url = storage.save(relPath, filename, file);


        // 포스터일 때 Movie.posterUrl 자동 반영
        if (type == MovieMediaType.POSTER) {
            movie.setPosterUrl(url);
            movieRepository.save(movie);
        }

        MovieMedia media = new MovieMedia();
        media.setMovie(movie);
        media.setMovieMediaType(type);
        media.setUrl(url);
        media.setDescription(description);

        return mediaRepository.save(media);
    }

    // 트레일러 URL 등록 (파일 업로드 없음)
    @Transactional
    public MovieMedia addTrailerUrl(Long movieId, String rawUrl, @Nullable String description) {
        String embed = toYoutubeEmbedUrl(rawUrl); // 유튜브면 embed URL로 치환
        MovieMedia media = new MovieMedia();
        media.setMovie(movieRepo.getReferenceById(movieId));
        media.setMovieMediaType(MovieMediaType.TRAILER);
        media.setUrl(embed != null ? embed : rawUrl); // 유튜브 외에는 원본 유지(비메오 등)
        media.setDescription(description);
        return movieMediaRepo.save(media);
    }

    /** 다양한 유튜브 URL -> https://www.youtube.com/embed/{id} 로 변환 */
    @Nullable
    private String toYoutubeEmbedUrl(String url) {
        if (url == null) return null;
        String lower = url.toLowerCase();

        // youtu.be/{id}
        if (lower.contains("youtu.be/")) {
            String id = url.substring(url.indexOf("youtu.be/") + "youtu.be/".length())
                    .split("[?&/#]")[0];
            return "https://www.youtube.com/embed/" + id;
        }
        // www.youtube.com/watch?v={id}&...
        if (lower.contains("youtube.com/watch")) {
            String query = url.substring(url.indexOf('?') + 1);
            for (String kv : query.split("&")) {
                String[] p = kv.split("=");
                if (p.length == 2 && p[0].equals("v")) {
                    return "https://www.youtube.com/embed/" + p[1];
                }
            }
        }
        // shorts/{id}
        if (lower.contains("youtube.com/shorts/")) {
            String id = url.substring(url.indexOf("/shorts/") + "/shorts/".length())
                    .split("[?&/#]")[0];
            return "https://www.youtube.com/embed/" + id;
        }
        return null; // 비유튜브는 변환하지 않음
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