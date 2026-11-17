package com.shopping.repository;

import com.shopping.entity.RecommendationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {

    Page<RecommendationLog> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
