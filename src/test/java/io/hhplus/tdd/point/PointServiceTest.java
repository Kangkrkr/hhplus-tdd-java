package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    // PointService 가 아래 두 객체에 대한 의존성을 가질 것이므로 @InjectMocks 사용
    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;

    final long EMPTY_POINT = 0L;

    @Test
    @DisplayName("유효한 USER ID 가 입력된 경우, 정상적으로 회원의 보유 포인트가 조회 되어야 한다.")
    public void givenExistentUserId_whenCallingFindByUserId_thenReturnsUserPoint() {

        final long userId = 1L;
        final long point = 500L;
        UserPoint expectedUserPoint = new UserPoint(userId, point, System.currentTimeMillis());
        // 정상적으로 조회된 회원 포인트 데이터 생성을 위한 STUB 객체가 생성 되도록..
        Mockito.when(userPointTable.selectById(userId)).thenReturn(expectedUserPoint);

        // 실제 pointService 테스트 객체로부터 UserPoint 조회
        UserPoint actualUserPoint = pointService.findUserPointByUserId(userId);

        // pointService 가 userPointTable 로 부터 조회해온 객체는 null 일 수 없음을 확인
        Assertions.assertNotNull(actualUserPoint);
        // 조회된 id, point 가 같아야 함
        Assertions.assertEquals(expectedUserPoint.id(), actualUserPoint.id());
        Assertions.assertEquals(expectedUserPoint.point(), actualUserPoint.point());
    }

    @Test
    @DisplayName("존재하지 않는 USER ID 가 입력된 경우에 대한 테스트")
    public void givenNonExistentUserId_whenCallingFindByUserId_thenReturnsEmptyUserPoint() {

        final long nonExistentUserId = 999L;

        // 존재 하지 않는 회원에 대해서는 empty 정적 팩토리 메소드 호출을 통한 빈 데이터가 생성 되어야 하므로
        // 아래와 같은 STUB 데이터 생성 처리..!
        UserPoint emptyUserPoint = UserPoint.empty(nonExistentUserId);
        Mockito.when(userPointTable.selectById(nonExistentUserId)).thenReturn(emptyUserPoint);

        UserPoint actualUserPoint = pointService.findUserPointByUserId(nonExistentUserId);

        // 데이터 검증
        Assertions.assertNotNull(actualUserPoint);
        Assertions.assertEquals(emptyUserPoint.id(), actualUserPoint.id());
        // 해당 회원 객체의 포인트는 0 이어야 한다.
        Assertions.assertEquals(EMPTY_POINT, actualUserPoint.point() );
    }

    @ParameterizedTest
    @MethodSource("providePointHistoryStubs")
    @DisplayName("회원 ID 가 주어진 경우, 이에 맞는 PointHistory 목록을 반환 한다.")
    public void givenExistentUserId_whenCallingFindAllPointHistoryByUserId_thenReturnsMatchingPointHistories(final List<PointHistory> pointHistoryStubs) {
        final long userId = 1L;

        Mockito.when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(pointHistoryStubs);

        List<PointHistory> actualPointHistories = pointService.findAllPointHistoryByUserId(userId);

        // 실제 조회된 객체는 null 일 수 없으므로 not null 체크
        Assertions.assertNotNull(actualPointHistories);
        // 두 객체 간의 size 비교 (일치 해야함)
        Assertions.assertEquals(pointHistoryStubs.size(), actualPointHistories.size());
        // PointHistory가 record 타입이므로 List 전체를 아래와 같이 비교..
        Assertions.assertEquals(pointHistoryStubs, actualPointHistories);
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
