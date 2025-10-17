package io.hhplus.tdd.point;

import lombok.Builder;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    @Builder(builderMethodName = "of")
    public UserPoint(long id, long point, long updateMillis) {
        this.id = id;
        this.point = point;
        this.updateMillis = updateMillis;
    }

    public static UserPoint empty(long id) {
        return UserPoint.of()
                .id(id)
                .point(0)
                .updateMillis(System.currentTimeMillis())
                .build();
    }
}
