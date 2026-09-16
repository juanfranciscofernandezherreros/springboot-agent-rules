package com.acme.orders.order.dto;

import com.acme.orders.order.model.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PatchOrderDto(
        @Size(min = 1, max = 100) String customerReference,
        OrderStatus status,
        @DecimalMin(value = "0.00", inclusive = false) BigDecimal totalAmount) {}
