package org.example;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

public class OrderBookImpl implements OrderBook {

    private final Map<Double, Set<TimedOrder>> bidLevels;
    private final Map<Double, Set<TimedOrder>> offerLevels;
    private final Map<Long, TimedOrder> orders;

    public OrderBookImpl() {
        bidLevels = new TreeMap<>(Comparator.reverseOrder());
        offerLevels = new TreeMap<>(Comparator.naturalOrder());
        orders = new HashMap<>();
    }

    @Override
    public void add(Order order) {
        orders.computeIfAbsent(order.getId(), id -> {
            TimedOrder newOrder = new TimedOrder(order, System.nanoTime());
            getOrdersForSide(order.getSide())
                    .computeIfAbsent(order.getPrice(),
                            k -> new TreeSet<>(comparing(TimedOrder::nanoTimeStamp, Long::compareTo)))
                    .add(newOrder);
            return newOrder;
        });
    }

    @Override
    public void remove(long id) {
        TimedOrder toRemove = orders.remove(id);
        if (nonNull(toRemove)) {
            Map<Double, Set<TimedOrder>> side = getOrdersForSide(toRemove.order().getSide());
            Set<TimedOrder> ordersAtLevel = side.get(toRemove.order().getPrice());
            ordersAtLevel.remove(toRemove);
            if (ordersAtLevel.isEmpty()) {
                side.remove(toRemove.order().getPrice());
            }
        }
    }

    @Override
    public void updateSize(long id, long newSize) {
        TimedOrder toUpdate = orders.get(id);
        if (nonNull(toUpdate)) {
            TimedOrder newOrder = toUpdate.withNewSize(newSize);
            Set<TimedOrder> ordersAtLevel = getOrdersForSide(toUpdate.order().getSide()).get(toUpdate.order().getPrice());
            orders.put(toUpdate.order().getId(), newOrder);
            ordersAtLevel.remove(toUpdate);
            ordersAtLevel.add(newOrder);
        }
    }

    @Override
    public double getPriceAtLevel(char side, int level) {
        var orderLevels = getOrdersForSide(side);
        if (level >= 1 && level <= orderLevels.size()) {
            return orderLevels.keySet().stream().skip(level - 1).findFirst().orElse(Double.NaN);
        } else {
            return Double.NaN;
        }
    }

    @Override
    public long getSizeAtLevel(char side, int level) {
        var orderLevels = getOrdersForSide(side);
        if (level >= 1 && level <= orderLevels.size()) {
            return orderLevels.values().stream().skip(level - 1).findFirst()
                    .flatMap(orderSet -> orderSet.stream().map(order -> order.order().getSize())
                            .reduce(Long::sum)).orElse(0L);
        } else {
            return 0;
        }
    }

    @Override
    public List<Order> getOrders(char side) {
        return getOrdersForSide(side).values().stream().
                flatMap(Set::stream).map(TimedOrder::order).toList();
    }

    private Map<Double, Set<TimedOrder>> getOrdersForSide(char side) {
        return switch (side) {
            case 'B' -> bidLevels;
            case 'O' -> offerLevels;
            default -> throw new IllegalArgumentException("'B' and 'O' are only permitted values for Order side");
        };
    }

    private record TimedOrder(Order order, long nanoTimeStamp) {
        public TimedOrder withNewSize(long newSize) {
            return new TimedOrder(
                    new Order(order.getId(), order.getPrice(), order.getSide(), newSize),
                    this.nanoTimeStamp
            );
        }
    }
}
