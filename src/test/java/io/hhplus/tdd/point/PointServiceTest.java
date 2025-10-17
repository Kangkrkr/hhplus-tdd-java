package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.IllegalPointException;
import io.hhplus.tdd.exception.InsufficientPointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    // PointService 가 아래 두 객체에 대한 의존성을 가질 것이므로 @InjectMocks 사용
    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;


    @Test
    @DisplayName("유효한 USER ID 가 입력된 경우, 정상적으로 UserPoint 가 조회 되어야 한다.")
    public void givenExistentUserId_whenCallingFindByUserId_thenReturnsUserPoint() {

        final long userId = 1L;
        final long point = 500L;
        UserPoint expectedUserPoint = new UserPoint(userId, point, System.currentTimeMillis());
        // 정상적으로 조회된 회원 포인트 데이터 생성을 위한 STUB 객체가 생성 되도록..
        when(userPointTable.selectById(userId)).thenReturn(expectedUserPoint);

        // 실제 pointService 테스트 객체로부터 UserPoint 조회
        UserPoint actualUserPoint = pointService.findUserPointByUserId(userId);

        // pointService 가 userPointTable 로 부터 조회해온 객체는 null 일 수 없음을 확인
        assertThat(actualUserPoint).isNotNull();
        // 조회된 id, point 가 같아야 함
        assertThat(actualUserPoint.id()).isEqualTo(expectedUserPoint.id());
        assertThat(actualUserPoint.point()).isEqualTo(expectedUserPoint.point());
    }

    @Test
    @DisplayName("존재하지 않는 USER ID 가 입력된 경우에 대한 테스트")
    public void givenNonExistentUserId_whenCallingFindByUserId_thenReturnsEmptyUserPoint() {

        final long nonExistentUserId = 999L;

        // 존재 하지 않는 회원에 대해서는 empty 정적 팩토리 메소드 호출을 통한 빈 데이터가 생성 되어야 하므로
        // 아래와 같은 STUB 데이터 생성 처리..!
        UserPoint emptyUserPoint = UserPoint.empty(nonExistentUserId);
        when(userPointTable.selectById(nonExistentUserId)).thenReturn(emptyUserPoint);

        UserPoint actualUserPoint = pointService.findUserPointByUserId(nonExistentUserId);

        // 데이터 검증
        assertThat(actualUserPoint).isNotNull();
        assertThat(actualUserPoint.id()).isEqualTo(emptyUserPoint.id());
        // 해당 회원 객체의 point는 0 이어야 한다.
        assertThat(actualUserPoint.point()).isEqualTo(emptyUserPoint.point());
    }

    @ParameterizedTest
    @MethodSource("providePointHistoryStubs")
    @DisplayName("회원 ID 가 주어진 경우, 이에 맞는 PointHistory 목록을 반환 한다.")
    public void givenExistentUserId_whenCallingFindAllPointHistoryByUserId_thenReturnsMatchingPointHistories(final List<PointHistory> pointHistoryStubs) {
        final long userId = 1L;

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(pointHistoryStubs);

        List<PointHistory> actualPointHistories = pointService.findAllPointHistoryByUserId(userId);

        // 실제 조회된 객체는 null 일 수 없으므로 not null 체크
        assertThat(actualPointHistories).isNotNull();
        // 두 객체 간의 size 비교 (일치 해야함)
        assertThat(actualPointHistories.size()).isEqualTo(pointHistoryStubs.size());
        // PointHistory가 record 타입이므로 List 전체를 아래와 같이 비교..
        assertThat(actualPointHistories).hasSameElementsAs(pointHistoryStubs);
    }

    @Test
    @DisplayName("userId 와 pointAmount 가 정상적으로 주어진 경우, 포인트 충전을 위해 chargePoint 호출 시 정상적으로 UserPoint 객체를 반환 해야한다.")
    public void givenUserIdAndPointAmount_whenCallingChargePoint_thenReturnsUserPoint() throws IllegalPointException {
        // given
        long userId = 1L;
        long currentPoint = 500L;
        long pointToCharge = 500L;
        long chargedPoint = currentPoint + pointToCharge;

        UserPoint foundUserPoint = new UserPoint(userId, currentPoint, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(foundUserPoint);

        UserPoint newUserPoint = new UserPoint(userId, chargedPoint, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(eq(userId), eq(chargedPoint))).thenReturn(newUserPoint);
        when(pointHistoryTable.insert(eq(userId), eq(pointToCharge), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(new PointHistory(1L, userId, pointToCharge, TransactionType.CHARGE, System.currentTimeMillis()));

        // when
        UserPoint actualUserPoint = pointService.chargePoint(userId, pointToCharge);

        // then
        assertThat(actualUserPoint).isNotNull();
        assertThat(actualUserPoint.id()).isEqualTo(newUserPoint.id());
        assertThat(actualUserPoint.point()).isEqualTo(newUserPoint.point());

        // 행위 검증
        verify(userPointTable).insertOrUpdate(userId, chargedPoint);
        verify(pointHistoryTable).insert(eq(userId), eq(pointToCharge), eq(TransactionType.CHARGE), anyLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -100L})
    @DisplayName("충전할 포인트가 0 이하(유효하지 않은) 경우, 예외를 발생시켜야 한다.")
    public void givenInvalidPointToCharge_whenCallingChargePoint_thenThrowsException(final long pointToCharge) {
        // given
        long userId = 1L;

        assertThatThrownBy(() -> pointService.chargePoint(userId, pointToCharge))
                .isInstanceOf(IllegalPointException.class)
                .hasMessage("충전할 포인트는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("userId 와 pointAmount 가 정상적으로 주어진 경우, 포인트 사용을 위해 usePoint 호출 시 정상적으로 UserPoint 객체를 반환 해야한다.")
    public void givenUserIdAndPointAmount_whenCallingUsePoint_thenReturnsUserPoint() throws InsufficientPointException, IllegalPointException {
        // given
        final long userId = 1L;
        final long currentPoint = 500L;
        final long pointToUse = 300L;
        final long leftPoint = currentPoint - pointToUse;

        UserPoint foundUserPoint = new UserPoint(userId, currentPoint, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(foundUserPoint);

        UserPoint newUserPoint = new UserPoint(userId, leftPoint, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(eq(userId), eq(leftPoint))).thenReturn(newUserPoint);
        when(pointHistoryTable.insert(eq(userId), eq(pointToUse), eq(TransactionType.USE), anyLong()))
                .thenReturn(new PointHistory(1L, userId, pointToUse, TransactionType.USE, System.currentTimeMillis()));

        // when
        UserPoint actualUserPoint = pointService.usePoint(userId, pointToUse);

        // then
        assertThat(actualUserPoint).isNotNull();
        assertThat(actualUserPoint.id()).isEqualTo(newUserPoint.id());
        assertThat(actualUserPoint.point()).isEqualTo(newUserPoint.point());

        // 행위 검증
        verify(userPointTable).insertOrUpdate(userId, leftPoint);
        verify(pointHistoryTable).insert(eq(userId), eq(pointToUse), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 시, 잔고가 부족한 경우 예외가 발생 하여야 한다.")
    public void givenPointToUseIsBiggerThenCurrentPoint_whenCallingUsePoint_thenThrowsException() {
        // given
        final long userId = 1L;
        final long currentPoint = 500L;
        final long pointToUse = 600L;

        UserPoint foundUserPoint = new UserPoint(userId, currentPoint, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(foundUserPoint);

        // when & then
        // pointService.usePoint를 실행했을 때,
        assertThatThrownBy(() -> pointService.usePoint(userId, pointToUse))
                .isInstanceOf(InsufficientPointException.class)   // Exception 타입의 예외가 발생 해야 하고,
                .hasMessage("잔고가 부족 합니다."); // 예외 메시지가 "잔고가 부족 합니다." 여야 한다.
    }

    @ParameterizedTest
    @ValueSource(longs = { 0L, -100L })
    @DisplayName("포인트 사용 시, 사용 하고자 하는 포인트 금액이 0 이하인 경우(유효하지 않은) 예외가 발생 하여야 한다.")
    public void givenInvalidPointToUse_whenCallingUsePoint_thenThrowsException(final long pointToUse) {
        // given
        final long userId = 1L;

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(userId, pointToUse))
                .isInstanceOf(IllegalPointException.class)
                .hasMessage("사용할 포인트는 0 이상이어야 합니다.");
    }

   private static Stream<Arguments> providePointHistoryStubs() {
       final long userId = 1L;
       List<PointHistory> historyStubs = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 200L, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(3L, userId, 500L, TransactionType.CHARGE, System.currentTimeMillis())
        );
        return Stream.of(Arguments.of(historyStubs));
    }

}
