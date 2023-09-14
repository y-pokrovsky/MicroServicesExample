package com.micro.services.example.orderservice.service;

import com.micro.services.example.orderservice.dto.InventoryResponse;
import com.micro.services.example.orderservice.dto.OrderLineItemsDto;
import com.micro.services.example.orderservice.dto.OrderRequest;
import com.micro.services.example.orderservice.model.Order;
import com.micro.services.example.orderservice.model.OrderLineItems;
import com.micro.services.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // вызываем inventory-service и, если товар присутствует на складе, размещаем заказ
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponse::isInStock);

        if (allProductsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Товар отсутствует на складе, пожалуйста, закажите позже");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLinesItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLinesItemsDto.getPrice());
        orderLineItems.setQuantity(orderLinesItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLinesItemsDto.getSkuCode());
        return orderLineItems;
    }
}
