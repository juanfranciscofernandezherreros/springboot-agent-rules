package com.acme.orders.order.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Order {

    private Long id;
    private String customerReference;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
}
