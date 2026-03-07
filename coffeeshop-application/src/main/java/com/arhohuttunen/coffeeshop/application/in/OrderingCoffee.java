package com.arhohuttunen.coffeeshop.application.in;

import com.arhohuttunen.coffeeshop.application.order.LineItem;
import com.arhohuttunen.coffeeshop.application.order.Order;
import com.arhohuttunen.coffeeshop.application.payment.CreditCard;
import com.arhohuttunen.coffeeshop.application.payment.Payment;
import com.arhohuttunen.coffeeshop.application.payment.Receipt;
import com.arhohuttunen.coffeeshop.shared.Location;

import java.util.List;
import java.util.UUID;

public interface OrderingCoffee {
    Order placeOrder(Location location, List<LineItem> items);
    Order updateOrder(UUID orderId, Location location, List<LineItem> items);
    void cancelOrder(UUID orderId);
    Payment payOrder(UUID orderId, CreditCard creditCard);
    Receipt readReceipt(UUID orderId);
    Order takeOrder(UUID orderId);
}
