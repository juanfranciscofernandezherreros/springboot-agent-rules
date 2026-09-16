package com.acme.orders.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.orders.exception.AppException;
import com.acme.orders.order.entity.OrderEntity;
import com.acme.orders.order.model.Order;
import com.acme.orders.order.model.OrderStatus;
import com.acme.orders.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void create_assigns_created_at_and_persists_order_ok() {
        // given
        var order = Order.builder()
                .withCustomerReference("customer-1")
                .withStatus(OrderStatus.PENDING)
                .withTotalAmount(new BigDecimal("25.00"))
                .build();
        var saved = entity(1L, "customer-1", OrderStatus.PENDING, "25.00");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(saved);

        // when
        var created = orderService.create(order);

        // then
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getCreatedAt()).isNotNull();
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void get_by_id_throws_when_order_does_not_exist_ko() {
        // given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> orderService.getById(99L)).isInstanceOf(AppException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void patch_updates_only_provided_fields_ok() {
        // given
        var existing = entity(1L, "customer-1", OrderStatus.PENDING, "25.00");
        var patch = Order.builder().withStatus(OrderStatus.CONFIRMED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(existing);

        // when
        var updated = orderService.patch(1L, patch);

        // then
        assertThat(updated.getCustomerReference()).isEqualTo("customer-1");
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(updated.getTotalAmount()).isEqualByComparingTo("25.00");
    }

    private OrderEntity entity(Long id, String reference, OrderStatus status, String amount) {
        var entity = new OrderEntity();
        entity.setId(id);
        entity.setCustomerReference(reference);
        entity.setStatus(status);
        entity.setTotalAmount(new BigDecimal(amount));
        entity.setCreatedAt(java.time.Instant.now());

        return entity;
    }
}
