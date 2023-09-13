package com.micro.services.example.orderservice.service;

import com.micro.services.example.orderservice.dto.OrderLineItemsDto;
import com.micro.services.example.orderservice.dto.OrderRequest;
import com.micro.services.example.orderservice.model.Order;
import com.micro.services.example.orderservice.model.OrderLineItems;
import com.micro.services.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLinesItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLinesItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLinesItemsDto.getPrice());
        orderLineItems.setQuantity(orderLinesItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLinesItemsDto.getSkuCode());
        return orderLineItems;
    }
}
