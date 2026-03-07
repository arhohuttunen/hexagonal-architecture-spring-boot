package com.arhohuttunen.coffeeshop.adapter.in.rest;

import com.arhohuttunen.coffeeshop.adapter.in.rest.resource.OrderRequest;
import com.arhohuttunen.coffeeshop.adapter.in.rest.resource.OrderResponse;
import com.arhohuttunen.coffeeshop.application.in.OrderingCoffee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderingCoffee orderingCoffee;

    @PostMapping("/order")
    ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request, UriComponentsBuilder uriComponentsBuilder) {
        var order = orderingCoffee.placeOrder(
                request.location(),
                request.toDomainItems()
        );
        var location = uriComponentsBuilder.path("/order/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(OrderResponse.fromDomain(order));
    }

    @PostMapping("/order/{id}")
    ResponseEntity<OrderResponse> updateOrder(@PathVariable UUID id, @RequestBody OrderRequest request) {
        var order = orderingCoffee.updateOrder(
                id,
                request.location(),
                request.toDomainItems()
        );
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    @DeleteMapping("/order/{id}")
    ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        orderingCoffee.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
