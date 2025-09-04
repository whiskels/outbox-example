package com.whiskels.order.api.repository;

import com.whiskels.order.api.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface OrderRepository extends JpaRepository<Order, UUID> {
}
