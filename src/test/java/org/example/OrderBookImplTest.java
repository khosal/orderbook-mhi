package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderBookImplTest {

    private OrderBookImpl orderBook;

    private final List<Order> bidOrders = List.of(
            new Order(11, 1.45, 'B', 10),
            new Order(12, 1.35, 'B', 20),
            new Order(13, 1.35, 'B', 30),
            new Order(14, 1.35, 'B', 40),
            new Order(15, 1.25, 'B', 50),
            new Order(16, 1.25, 'B', 60)
    );

    private final List<Order> offerOrders = List.of(
            new Order(21, 1.55, 'O', 30),
            new Order(22, 1.65, 'O', 40),
            new Order(23, 1.65, 'O', 50),
            new Order(24, 1.65, 'O', 60),
            new Order(25, 1.75, 'O', 70),
            new Order(26, 1.75, 'O', 80)
    );

    @BeforeEach
    public void setup() {
        orderBook = new OrderBookImpl();
        bidOrders.forEach(orderBook::add);
        offerOrders.forEach(orderBook::add);
    }

    @Test
    public void whenOrdersAdded_thenBothSidesOfBookHaveExpectedOrder() {
        assertEquals(bidOrders, orderBook.getOrders('B'));
        assertEquals(offerOrders, orderBook.getOrders('O'));
    }

    @Test
    public void whenOrdersWithSameIdAddedAgain_thenBookUnaffected() {
        orderBook.add(bidOrders.get(1));
        orderBook.add(new Order(13, 1.95, 'B', 55));

        assertEquals(bidOrders, orderBook.getOrders('B'));
    }

    @Test
    public void whenOrderSizeChanged_thenSizeUpdatedWithoutModifyingPriority() {
        orderBook.updateSize(13, 95);
        assertEquals(95, orderBook.getOrders('B').get(2).getSize());
    }

    @Test
    public void whenOrderSizeChanged_thenSizeAtLevelUpdatedAsExpected() {
        orderBook.updateSize(12, 120);
        assertEquals(190, orderBook.getSizeAtLevel('B', 2));
    }

    @Test
    public void whenOrdersRemoved_thenBothSidesOfBookAsExpected() {
        orderBook.remove(11);
        orderBook.remove(13);

        List<Order> expectedBidOrders = IntStream.of(1, 3, 4, 5).mapToObj(bidOrders::get).toList();
        assertEquals(expectedBidOrders, orderBook.getOrders('B'));

        orderBook.remove(24);
        orderBook.remove(25);

        List<Order> expectedOfferOrders = IntStream.of(0, 1, 2, 5).mapToObj(offerOrders::get).toList();
        assertEquals(expectedOfferOrders, orderBook.getOrders('O'));
    }

    @Test
    public void whenNonExistentOrdersRemoved_thenBookUnchanged() {
        orderBook.remove(99);
        orderBook.remove(999);
        assertEquals(bidOrders, orderBook.getOrders('B'));
        assertEquals(offerOrders, orderBook.getOrders('O'));
    }

    @Test
    public void whenNonExistentOrderSizesModified_thenBookUnchanged() {
        orderBook.updateSize(99, 120);
        orderBook.updateSize(999, 125);
        assertEquals(bidOrders, orderBook.getOrders('B'));
        assertEquals(offerOrders, orderBook.getOrders('O'));
    }

    @Test
    public void whenPriceAtLevel_thenExpectedValues() {
        assertEquals(Double.NaN, orderBook.getPriceAtLevel('B', -5));
        assertEquals(Double.NaN, orderBook.getPriceAtLevel('B', 0));
        assertEquals(1.45, orderBook.getPriceAtLevel('B', 1));
        assertEquals(1.35, orderBook.getPriceAtLevel('B', 2));
        assertEquals(1.25, orderBook.getPriceAtLevel('B', 3));
        assertEquals(Double.NaN, orderBook.getPriceAtLevel('B', 4));

        assertEquals(Double.NaN, orderBook.getPriceAtLevel('O', -5));
        assertEquals(Double.NaN, orderBook.getPriceAtLevel('O', 0));
        assertEquals(1.55, orderBook.getPriceAtLevel('O', 1));
        assertEquals(1.65, orderBook.getPriceAtLevel('O', 2));
        assertEquals(1.75, orderBook.getPriceAtLevel('O', 3));
        assertEquals(Double.NaN, orderBook.getPriceAtLevel('O', 4));
    }

    @Test
    public void whenSizeAtLevel_thenExpectedValues() {
        assertEquals(0, orderBook.getSizeAtLevel('B', -5));
        assertEquals(0, orderBook.getSizeAtLevel('B', 0));
        assertEquals(10, orderBook.getSizeAtLevel('B', 1));
        assertEquals(90, orderBook.getSizeAtLevel('B', 2));
        assertEquals(110, orderBook.getSizeAtLevel('B', 3));
        assertEquals(0, orderBook.getSizeAtLevel('B', 4));

        assertEquals(0, orderBook.getSizeAtLevel('O', -5));
        assertEquals(0, orderBook.getSizeAtLevel('O', 0));
        assertEquals(30, orderBook.getSizeAtLevel('O', 1));
        assertEquals(150, orderBook.getSizeAtLevel('O', 2));
        assertEquals(150, orderBook.getSizeAtLevel('O', 3));
        assertEquals(0, orderBook.getSizeAtLevel('O', 4));
    }

    @Test
    public void whenOrdersRemoved_thenLevelsShiftAsExpected() {
        assertEquals(1.55, orderBook.getPriceAtLevel('O', 1));
        orderBook.remove(21);
        assertEquals(1.65, orderBook.getPriceAtLevel('O', 1));
        orderBook.remove(22);
        orderBook.remove(23);
        orderBook.remove(24);
        assertEquals(1.75, orderBook.getPriceAtLevel('O', 1));
        orderBook.remove(25);
        orderBook.remove(26);
        assertEquals(Double.NaN, orderBook.getPriceAtLevel('O', 1));
        assertEquals(List.of(), orderBook.getOrders('O'));
    }

}
