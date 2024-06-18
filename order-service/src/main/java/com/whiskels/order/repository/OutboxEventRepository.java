package com.whiskels.order.repository;

import com.whiskels.order.entity.OutboxEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends CrudRepository<OutboxEvent, UUID> {
    @Query("from OutboxEvent where sent is null order by created asc limit 10")
    List<OutboxEvent> findNotSentBatch();
}
