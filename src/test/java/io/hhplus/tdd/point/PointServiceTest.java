package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    // PointService 가 아래 두 객체에 대한 의존성을 가질 것이므로 @InjectMocks 사용
    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistory pointHistory;

    final long EMPTY_POINT = 0L;

    @Test
    @DisplayName("유효한 USER ID 가 입력된 경우, 정상적으로 회원의 보유 포인트가 조회 되어야 한다.")
    public void givenUserId_whenCallingFindByUserId_thenReturnsUserPoint() {

        final long userId = 1L;
        final long point = 500L;
        UserPoint expectedUserPoint = new UserPoint(userId, point, System.currentTimeMillis());
        // 정상적으로 조회된 회원 포인트 데이터 생성을 위한 STUB 객체가 생성 되도록..
        Mockito.when(userPointTable.selectById(userId)).thenReturn(expectedUserPoint);

        // 실제 pointService 테스트 객체로부터 UserPoint 조회
        UserPoint actualUserPoint = pointService.findUserPointById(userId);

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

        UserPoint actualUserPoint = pointService.findUserPointById(nonExistentUserId);

        // 데이터 검증
        Assertions.assertNotNull(actualUserPoint);
        Assertions.assertEquals(emptyUserPoint.id(), actualUserPoint.id());
        // 해당 회원의 포인트는 0 이어야 한다.
        Assertions.assertEquals(EMPTY_POINT, actualUserPoint.point() );
    }

}
