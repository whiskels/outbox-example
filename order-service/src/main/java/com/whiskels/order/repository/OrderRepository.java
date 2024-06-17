package com.whiskels.order.repository;

import com.whiskels.order.entity.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface OrderRepository extends CrudRepository<Order, UUID> {
}
