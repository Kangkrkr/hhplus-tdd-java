package io.hhplus.tdd.point;

import lombok.Builder;

public record UserPoint(
        long id,
        Point point,
        long updateMillis
) {

    @Builder(builderMethodName = "of")
    public UserPoint(long id, Point point, long updateMillis) {
        this.id = id;
        this.point = point;
        this.updateMillis = updateMillis;
    }

    public static UserPoint empty(long id) {
        return UserPoint.of()
                .id(id)
                .point(Point.ZERO)
                .updateMillis(System.currentTimeMillis())
                .build();
    }
}
