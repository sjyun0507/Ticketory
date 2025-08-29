package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.enums.Type;
import com.gudrhs8304.ticketory.dto.board.*;
import com.gudrhs8304.ticketory.service.BoardPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BoardPostController {

    private final BoardPostService service;

    /** 공개 조회 (사용자 페이지) */
    @GetMapping("/board")
    public ResponseEntity<Page<BoardPostRes>> publicList(
            @RequestParam(required=false, name="type") String typeCsv,
            @RequestParam(required=false) Boolean published,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="12") int size
    ) {
        List<Type> types = null;
        if (typeCsv != null && !typeCsv.isBlank()) {
            types = Arrays.stream(typeCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .map(Type::valueOf)
                    .toList();
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(service.list(types, published, pageable));
    }

    /** 생성 (관리자) */
    @PostMapping("/admin/board")
    public ResponseEntity<BoardPostRes> create(@Valid @RequestBody BoardPostReq req) {
        return ResponseEntity.ok(service.create(req));
    }

    /** 수정 (관리자) */
    @PutMapping("/admin/board/{id}")
    public ResponseEntity<BoardPostRes> update(@PathVariable Long id,
                                               @Valid @RequestBody BoardPostReq req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    /** 삭제 (관리자) */
    @DeleteMapping("/admin/board/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
