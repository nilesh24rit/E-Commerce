package com.commercex.repository;

import com.commercex.entity.Order;
import com.commercex.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(UUID userId);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
}
