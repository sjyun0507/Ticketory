package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.BoardPost;
import com.gudrhs8304.ticketory.domain.enums.Type;
import com.gudrhs8304.ticketory.dto.board.*;
import com.gudrhs8304.ticketory.repository.BoardPostRepository;
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
                .published(req.published() == null ? Boolean.TRUE : req.published())
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
        e.setPublished(req.published() == null ? Boolean.TRUE : req.published());
        return toRes(repo.save(e));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private BoardPostRes toRes(BoardPost b) {
        return new BoardPostRes(
                b.getId(), b.getType(), b.getTitle(), b.getContent(), b.getBannerUrl(),
                b.getStartDate(), b.getEndDate(), b.getPublished(), b.getCreatedAt()
        );
    }
}
