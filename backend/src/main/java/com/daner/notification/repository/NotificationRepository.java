package com.daner.notification.repository;

import com.daner.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @org.springframework.data.jpa.repository.Query(
            "SELECT n FROM Notification n WHERE n.user.id = :userId " +
                    "ORDER BY n.createdAt DESC, n.id DESC")
    Slice<Notification> findByUserIdOrderByCreatedAtDesc(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    List<Notification> findAllByIdInAndUserId(Collection<Long> ids, Long userId);
}
