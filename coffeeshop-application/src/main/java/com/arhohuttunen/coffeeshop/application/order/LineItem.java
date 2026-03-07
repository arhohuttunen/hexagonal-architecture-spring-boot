package com.arhohuttunen.coffeeshop.application.order;

import com.arhohuttunen.coffeeshop.shared.Drink;
import com.arhohuttunen.coffeeshop.shared.Milk;
import com.arhohuttunen.coffeeshop.shared.Size;

import java.math.BigDecimal;

public record LineItem(Drink drink, Milk milk, Size size, int quantity) {
    // For simplicity every small drink costs 4.0 and large 5.0
    BigDecimal getCost() {
        var price = BigDecimal.valueOf(4.0);
        if (size == Size.LARGE) {
            price = BigDecimal.valueOf(5.0);
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
