package com.gudrhs8304.ticketory.feature.board;

import com.gudrhs8304.ticketory.feature.board.dto.BoardPostReq;
import com.gudrhs8304.ticketory.feature.board.dto.BoardPostRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository repo;

    public Page<BoardPostRes> list(List<Type> types, Boolean published, Pageable pageable) {
        var page = repo.findForList(types == null || types.isEmpty() ? null : types, published, pageable);
        return page.map(this::toRes);
    }

    public BoardPostRes create(BoardPostReq req) {
        var e = BoardPost.builder()
                .type(req.type())
                .title(req.title())
                .content(req.content())
                .bannerUrl(req.bannerUrl())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .publishAt(req.publishAt())
                .published(req.published() == null ? Boolean.TRUE : req.published()) // ★ 중요
                .build();
        return toRes(repo.save(e));
    }

    public BoardPostRes update(Long id, BoardPostReq req) {
        var e = repo.findById(id).orElseThrow();
        e.setType(req.type());
        e.setTitle(req.title());
        e.setContent(req.content());
        e.setBannerUrl(req.bannerUrl());
        e.setStartDate(req.startDate());
        e.setEndDate(req.endDate());
        e.setPublishAt(req.publishAt());
        e.setPublished(req.published() == null ? e.getPublished() : req.published()); // ★ 중요
        return toRes(repo.save(e));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private BoardPostRes toRes(BoardPost b) {
        return new BoardPostRes(
                b.getId(), b.getType(), b.getTitle(), b.getContent(), b.getBannerUrl(),
                b.getStartDate(), b.getEndDate(), b.getPublishAt(), b.getCreatedAt(), b.getPublished()
        );
    }
}
