package io.hhplus.tdd.point;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

public class Stubs {

    public static Stream<Arguments> providePointHistoryStubs() {
        final long userId = 1L;
        List<PointHistory> historyStubs = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 200L, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(3L, userId, 500L, TransactionType.CHARGE, System.currentTimeMillis())
        );
        return Stream.of(Arguments.of(historyStubs));
    }

}
