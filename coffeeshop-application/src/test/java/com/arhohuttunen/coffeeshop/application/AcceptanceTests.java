package com.arhohuttunen.coffeeshop.application;

import com.arhohuttunen.coffeeshop.application.in.OrderingCoffee;
import com.arhohuttunen.coffeeshop.application.in.PreparingCoffee;
import com.arhohuttunen.coffeeshop.application.order.LineItem;
import com.arhohuttunen.coffeeshop.application.out.OrderNotFound;
import com.arhohuttunen.coffeeshop.application.out.Orders;
import com.arhohuttunen.coffeeshop.application.out.Payments;
import com.arhohuttunen.coffeeshop.application.out.stub.InMemoryOrders;
import com.arhohuttunen.coffeeshop.application.out.stub.InMemoryPayments;
import com.arhohuttunen.coffeeshop.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.arhohuttunen.coffeeshop.application.order.OrderTestFactory.*;
import static com.arhohuttunen.coffeeshop.application.payment.CreditCardTestFactory.aCreditCard;
import static com.arhohuttunen.coffeeshop.application.payment.PaymentTestFactory.aPaymentForOrder;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcceptanceTests {
    private Orders orders;
    private Payments payments;
    private OrderingCoffee customer;
    private PreparingCoffee barista;

    @BeforeEach
    void setup() {
        orders = new InMemoryOrders();
        payments = new InMemoryPayments();
        customer = new CoffeeShop(orders, payments);
        barista = new CoffeeMachine(orders);
    }

    @Test
    void customerCanOrderCoffee() {
        var orderItems = List.of(new LineItem(Drink.CAPPUCCINO, Milk.SKIMMED, Size.SMALL, 1));

        var order = customer.placeOrder(Location.IN_STORE, orderItems);

        assertThat(order.getLocation()).isEqualTo(Location.IN_STORE);
        assertThat(order.getItems()).containsExactly(new LineItem(Drink.CAPPUCCINO, Milk.SKIMMED, Size.SMALL, 1));
        assertThat(order.getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
    }

    @Test
    void customerCanUpdateTheOrderBeforePaying() {
        var oneItem = List.of(new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1));
        var twoItems = List.of(new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 2));

        var order = customer.placeOrder(Location.TAKE_AWAY, oneItem);
        var updatedOrder = customer.updateOrder(order.getId(), Location.TAKE_AWAY, twoItems);

        assertThat(updatedOrder.getItems()).containsExactly(new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 2));
    }

    @Test
    void customerCanCancelTheOrderBeforePaying() {
        var existingOrder = orders.save(anOrder());

        customer.cancelOrder(existingOrder.getId());

        assertThatThrownBy(() -> orders.findOrderById(existingOrder.getId())).isInstanceOf(OrderNotFound.class);
    }

    @Test
    void customerCanPayTheOrder() {
        var existingOrder = orders.save(anOrder());
        var creditCard = aCreditCard();

        var payment = customer.payOrder(existingOrder.getId(), creditCard);

        assertThat(payment.orderId()).isEqualTo(existingOrder.getId());
        assertThat(payment.creditCard()).isEqualTo(creditCard);
        assertThat(orders.findOrderById(existingOrder.getId()).getStatus()).isEqualTo(Status.PAID);
    }

    @Test
    void noChangesAllowedWhenOrderIsPaid() {
        var existingOrder = orders.save(aPaidOrder());

        assertThatThrownBy(() -> customer.updateOrder(existingOrder.getId(), Location.TAKE_AWAY, emptyList())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void customerCanGetReceiptWhenOrderIsPaid() {
        var existingOrder = orders.save(aPaidOrder());
        var existingPayment = payments.save(aPaymentForOrder(existingOrder));

        var receipt = customer.readReceipt(existingOrder.getId());

        assertThat(receipt.amount()).isEqualTo(existingOrder.getCost());
        assertThat(receipt.paid()).isEqualTo(existingPayment.paid());
    }

    @Test
    void baristaCanStartPreparingTheOrderWhenItIsPaid() {
        var existingOrder = orders.save(aPaidOrder());

        var orderInPreparation = barista.startPreparingOrder(existingOrder.getId());

        assertThat(orderInPreparation.getStatus()).isEqualTo(Status.PREPARING);
    }

    @Test
    void baristaCanMarkTheOrderReadyWhenSheIsFinishedPreparing() {
        var existingOrder = orders.save(anOrderInPreparation());

        var preparedOrder = barista.finishPreparingOrder(existingOrder.getId());

        assertThat(preparedOrder.getStatus()).isEqualTo(Status.READY);
    }

    @Test
    void customerCanTakeTheOrderWhenItIsReady() {
        var existingOrder = orders.save(aReadyOrder());

        var takenOrder = customer.takeOrder(existingOrder.getId());

        assertThat(takenOrder.getStatus()).isEqualTo(Status.TAKEN);
    }
}
