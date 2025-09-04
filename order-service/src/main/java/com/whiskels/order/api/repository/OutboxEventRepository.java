package com.whiskels.order.api.repository;

import com.whiskels.order.api.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query("from OutboxEvent where sent is null order by created asc limit 10")
    List<OutboxEvent> findNotSentBatch();
}
