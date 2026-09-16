package com.acme.orders.order.repository;

import com.acme.orders.order.entity.OrderEntity;
import com.acme.orders.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findByCustomerReferenceContainingIgnoreCase(String customerReference, Pageable pageable);

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    Page<OrderEntity> findByCustomerReferenceContainingIgnoreCaseAndStatus(
            String customerReference, OrderStatus status, Pageable pageable);
}
