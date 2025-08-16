package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Page<Movie> findByStatus(Boolean status, Pageable pageable);
}
