package com.gudrhs8304.ticketory.feature.story;

import org.springframework.data.jpa.domain.Specification;

public class StorySpecs {

    public static Specification<Story> movieIdEq(Long movieId) {
        return (root, cq, cb) -> movieId == null ? null :
                cb.equal(root.get("movie").get("movieId"), movieId);
    }

    public static Specification<Story> memberIdEq(Long memberId) {
        return (root, cq, cb) -> memberId == null ? null :
                cb.equal(root.get("member").get("memberId"), memberId);
    }

    public static Specification<Story> statusActive() {
        return (root, cq, cb) -> cb.equal(root.get("status"), StoryStatus.ACTIVE);
    }
}
