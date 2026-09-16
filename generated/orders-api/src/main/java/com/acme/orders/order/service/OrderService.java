package com.acme.orders.order.service;

import com.acme.orders.order.model.Order;
import com.acme.orders.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Order create(Order order);

    Order getById(Long id);

    Page<Order> search(String customerReference, OrderStatus status, Pageable pageable);

    Order patch(Long id, Order order);

    void delete(Long id);
}
