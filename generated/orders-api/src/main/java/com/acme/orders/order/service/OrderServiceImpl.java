package com.acme.orders.order.service;

import com.acme.orders.exception.AppErrorMessage;
import com.acme.orders.exception.AppException;
import com.acme.orders.order.entity.OrderEntity;
import com.acme.orders.order.mapper.OrderEntityMapper;
import com.acme.orders.order.model.Order;
import com.acme.orders.order.model.OrderStatus;
import com.acme.orders.order.repository.OrderRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Order create(Order order) {
        order.setCreatedAt(Instant.now());
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        OrderEntity saved = orderRepository.save(entity);
        Order created = OrderEntityMapper.toModel(saved);
        log.info("[ORDER] - ACTION: createOrder: orderId: {}", created.getId());

        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(Long id) {
        OrderEntity entity = findEntityById(id);
        Order order = OrderEntityMapper.toModel(entity);

        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> search(String customerReference, OrderStatus status, Pageable pageable) {
        Page<OrderEntity> entities;
        if (customerReference != null && status != null) {
            entities = orderRepository.findByCustomerReferenceContainingIgnoreCaseAndStatus(
                    customerReference, status, pageable);
        } else if (customerReference != null) {
            entities = orderRepository.findByCustomerReferenceContainingIgnoreCase(customerReference, pageable);
        } else if (status != null) {
            entities = orderRepository.findByStatus(status, pageable);
        } else {
            entities = orderRepository.findAll(pageable);
        }
        Page<Order> orders = entities.map(OrderEntityMapper::toModel);

        return orders;
    }

    @Override
    public Order patch(Long id, Order patch) {
        OrderEntity entity = findEntityById(id);
        applyPatch(entity, patch);
        OrderEntity saved = orderRepository.save(entity);
        Order updated = OrderEntityMapper.toModel(saved);
        log.info("[ORDER] - ACTION: patchOrder: orderId: {}", updated.getId());

        return updated;
    }

    @Override
    public void delete(Long id) {
        OrderEntity entity = findEntityById(id);
        orderRepository.delete(entity);
        log.info("[ORDER] - ACTION: deleteOrder: orderId: {}", id);
    }

    private OrderEntity findEntityById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new AppException(AppErrorMessage.ORDER_NOT_FOUND));
    }

    private void applyPatch(OrderEntity entity, Order patch) {
        if (patch.getCustomerReference() != null) {
            entity.setCustomerReference(patch.getCustomerReference());
        }
        if (patch.getStatus() != null) {
            entity.setStatus(patch.getStatus());
        }
        if (patch.getTotalAmount() != null) {
            entity.setTotalAmount(patch.getTotalAmount());
        }
    }
}
