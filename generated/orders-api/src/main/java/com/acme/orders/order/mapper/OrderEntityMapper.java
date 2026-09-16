package com.acme.orders.order.mapper;

import com.acme.orders.order.entity.OrderEntity;
import com.acme.orders.order.model.Order;

public final class OrderEntityMapper {

    private OrderEntityMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setCustomerReference(order.getCustomerReference());
        entity.setStatus(order.getStatus());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setCreatedAt(order.getCreatedAt());

        return entity;
    }

    public static Order toModel(OrderEntity entity) {
        Order order = Order.builder()
                .withId(entity.getId())
                .withCustomerReference(entity.getCustomerReference())
                .withStatus(entity.getStatus())
                .withTotalAmount(entity.getTotalAmount())
                .withCreatedAt(entity.getCreatedAt())
                .build();

        return order;
    }
}
