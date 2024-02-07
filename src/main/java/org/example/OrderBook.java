package org.example;

import java.util.List;

public interface OrderBook {

    void add(Order order);
    void remove(long id);
    void updateSize(long id, long newSize);
    double getPriceAtLevel(char side, int level);
    long getSizeAtLevel(char side, int level);
    List<Order> getOrders(char side);

}
