package com.arhohuttunen.coffeeshop.adapter.in.rest.resource;

import com.arhohuttunen.coffeeshop.application.order.LineItem;
import com.arhohuttunen.coffeeshop.shared.Location;

import java.util.List;

public record OrderRequest(Location location, List<LineItemRequest> items) {
    public List<LineItem> toDomainItems() {
        return items.stream().map(LineItemRequest::toDomain).toList();
    }
}
