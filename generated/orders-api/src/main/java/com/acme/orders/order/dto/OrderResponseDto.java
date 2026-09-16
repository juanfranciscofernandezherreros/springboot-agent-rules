package com.acme.orders.order.dto;

import com.acme.orders.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponseDto(
        Long id, String customerReference, OrderStatus status, BigDecimal totalAmount, Instant createdAt) {}
