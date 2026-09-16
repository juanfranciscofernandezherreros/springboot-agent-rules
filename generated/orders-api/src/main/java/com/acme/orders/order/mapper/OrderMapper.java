package com.acme.orders.order.mapper;

import com.acme.orders.order.dto.CreateOrderDto;
import com.acme.orders.order.dto.OrderResponseDto;
import com.acme.orders.order.dto.PatchOrderDto;
import com.acme.orders.order.model.Order;

public final class OrderMapper {

    private OrderMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Order fromCreateDto(CreateOrderDto dto) {
        Order order = Order.builder()
                .withCustomerReference(dto.customerReference())
                .withStatus(dto.status())
                .withTotalAmount(dto.totalAmount())
                .build();

        return order;
    }

    public static Order fromPatchDto(PatchOrderDto dto) {
        Order order = Order.builder()
                .withCustomerReference(dto.customerReference())
                .withStatus(dto.status())
                .withTotalAmount(dto.totalAmount())
                .build();

        return order;
    }

    public static OrderResponseDto toDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto(
                order.getId(),
                order.getCustomerReference(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt());

        return dto;
    }
}
