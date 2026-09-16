package com.acme.orders.order.controller;

import com.acme.orders.order.dto.CreateOrderDto;
import com.acme.orders.order.dto.OrderResponseDto;
import com.acme.orders.order.dto.PatchOrderDto;
import com.acme.orders.order.mapper.OrderMapper;
import com.acme.orders.order.model.OrderStatus;
import com.acme.orders.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto create(@RequestBody @Valid CreateOrderDto dto) {
        var order = OrderMapper.fromCreateDto(dto);
        var created = orderService.create(order);
        var response = OrderMapper.toDto(created);

        return response;
    }

    @GetMapping("/{id}")
    public OrderResponseDto getById(@PathVariable Long id) {
        var order = orderService.getById(id);
        var response = OrderMapper.toDto(order);

        return response;
    }

    @GetMapping("/search")
    public Page<OrderResponseDto> search(
            @RequestParam(required = false) String customerReference,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        var orders = orderService.search(customerReference, status, pageable);
        var response = orders.map(OrderMapper::toDto);

        return response;
    }

    @PatchMapping("/{id}")
    public OrderResponseDto patch(@PathVariable Long id, @RequestBody @Valid PatchOrderDto dto) {
        var patch = OrderMapper.fromPatchDto(dto);
        var updated = orderService.patch(id, patch);
        var response = OrderMapper.toDto(updated);

        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}
