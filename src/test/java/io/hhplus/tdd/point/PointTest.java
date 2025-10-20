package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.IllegalPointException;
import io.hhplus.tdd.exception.InsufficientPointException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointTest {

    @Test
    @DisplayName("잘못된(0 미만의 음수 값) 포인트 입력에 대한 Point 객체 생성 시, IllegalPointException 을 던져애 한다.")
    public void givenIllegalPoint_whenCreatePoint_thenThrowsIllegalPointException() {
        final long point = -100L;

        Assertions.assertThatThrownBy(() -> {
                      Point.of()
                           .point(point)
                           .build();
                  }).isInstanceOf(IllegalPointException.class)
                  .hasMessage("유효하지 않은 포인트 입니다.");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 100L})
    @DisplayName("유효한 포인트 입력 시(0 이상), Point 도메인 객체 생성에 대해 성공 하여야 한다.")
    public void givenValidPoint_whenCreatePoint_thenReturnSuccess(final long expectedPoint) {

        Point actualPoint = Point.of()
                                 .point(expectedPoint)
                                 .build();

        assertThat(actualPoint).isNotNull();
        assertThat(actualPoint.getPoint()).isEqualTo(expectedPoint);
    }

    @Nested
    @DisplayName("충전(Charge)에 대한 도메인 행위 테스트")
    class Charge {

        @Test
        @DisplayName("충전할 Point 에 대해 음수 값이 주어진 경우 IllegalPointException 예외가 발생하여야 한다.")
        public void givenIllegalPoint_whenCallingCharge_thenThrowsIllegalPointException() {
            // given -> 100 원의 포인트를 가진 회원이 있다고 가정..
            Point currentPoint = Point.of()
                                      .point(100L)
                                      .build();

            assertThatThrownBy(() -> {
                // -100 원을 충전 시도 한다 ?
                currentPoint.charge(-100L);
            }).isInstanceOf(IllegalPointException.class)    // 예외 발생 !
              .hasMessage("충전할 포인트는 0 이상이어야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 100L, 200L})
        @DisplayName("유효한 충전 포인트가 주어진 경우(0 이상), 현재 포인트에 대해 정상적으로 충전된 Point 가 생성 및 반환 되어야 한다.")
        public void givenValidPoint_whenCallingCharge_thenReturnSuccess(final long pointToCharge) {
            // given -> 현재 회원의 포인트는 100 원, 충전할 금액은 50 원
            Point currentPoint = Point.of()
                                      .point(100L)
                                      .build();
            // 충전 후 150 원이 되어야 한다.
            final long expectedPoint = 100L + pointToCharge;

            Point chargedPoint = currentPoint.charge(pointToCharge);

            assertThat(chargedPoint).isNotNull();
            assertThat(chargedPoint.getPoint()).isEqualTo(expectedPoint);
        }
    }

    @Nested
    @DisplayName("사용(Use)에 대한 도메인 행위 테스트")
    class Use {

        @Test
        @DisplayName("사용할 Point 에 대해 음수 값이 주어진 경우 IllegalPointException 예외가 발생하여야 한다.")
        public void givenIllegalPoint_whenCallingUse_thenThrowsIllegalPointException() {
            Point currentPoint = Point.of()
                                      .point(100L)
                                      .build();

            assertThatThrownBy(() -> {
                // -100 원을 사용 시도 한다 ?
                currentPoint.use(-100L);
            }).isInstanceOf(IllegalPointException.class)
              .hasMessage("사용할 포인트는 0 이상이어야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 100L})
        @DisplayName("유효한 사용 포인트가 주어진 경우, 현재 포인트에 대해 정상적으로 사용된 Point 가 생성 및 반환 되어야 한다.")
        public void givenValidPoint_whenCallingUse_thenReturnSuccess(final long pointToUse) {
            // given -> 현재 회원의 포인트는 100 원, 사용할 금액은 50 원
            Point currentPoint = Point.of()
                                      .point(100L)
                                      .build();
            // 사용 후 50 원이 남아야 한다.
            final long expectedPoint = 100L - pointToUse;

            Point chargedPoint = currentPoint.use(pointToUse);
            assertThat(chargedPoint).isNotNull();
            assertThat(chargedPoint.getPoint()).isEqualTo(expectedPoint);
        }

        @Test
        @DisplayName("사용하고자 하는 포인트가 잔액 보다 많은 경우, 포인트 잔고 부족(InsufficientPointException) 가 발생 하여야 한다.")
        public void givenPointToUseGreaterThenCurrentPoint_whenCallingUse_thenThrowsInsufficientPointException() {
            // given -> 현재 회원의 포인트는 200 원, 사용할 금액은 300 원
            Point currentPoint = Point.of()
                                      .point(200L)
                                      .build();

            final long pointToUse = 300L;

            assertThatThrownBy(() -> {
                currentPoint.use(pointToUse);
            }).isInstanceOf(InsufficientPointException.class)
              .hasMessage("잔고가 부족 합니다.");
        }

    }

}