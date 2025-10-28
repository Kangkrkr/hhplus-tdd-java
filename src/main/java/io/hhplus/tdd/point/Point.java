package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.IllegalPointException;
import io.hhplus.tdd.exception.InsufficientPointException;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class Point {

    public static Point ZERO = Point.of(0);

    private final long point;

    public static Point of(final long point) {
        return Point.builder()
                    .point(point)
                    .build();
    }

    @Builder
    private Point(long point) {
        if (point < 0) {
            throw new IllegalPointException("유효하지 않은 포인트 입니다.");
        }

        this.point = point;
    }

    public Point charge(final long pointToCharge) {
        if (pointToCharge < 0) {
            throw new IllegalPointException("충전할 포인트는 0 이상이어야 합니다.");
        }

        return Point.of(this.point + pointToCharge);
    }

    public Point use(final long pointToUse) {
        if (pointToUse < 0) {
            throw new IllegalPointException("사용할 포인트는 0 이상이어야 합니다.");
        }

        final long currentPoint = this.point - pointToUse;
        if (currentPoint < 0) {
            throw new InsufficientPointException("잔고가 부족 합니다.");
        }

        return Point.of(currentPoint);
    }
}
