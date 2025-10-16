package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

// "포인트" 라는 개념과 업무/책임에 있어서 하나의 서비스로 작성 하는 것이
// 응집도 및 추후 트랜잭션 관리에도 유리할 것 같아, PointService 와 같은 단일 서비스로 작성..
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(final UserPointTable userPointTable,
                        final PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    public UserPoint findUserPointByUserId(final long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> findAllPointHistoryByUserId(final long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint chargePoint(final long userId, final long point) {
        // 충전 대상을 조회
        final UserPoint foundUserPoint = userPointTable.selectById(userId);

        // 기존 포인트에 충전 포인트를 합산..
        final long chargedPoint = foundUserPoint.point() + point;
        // 데이터 업데이트
        final UserPoint newUserPoint = userPointTable.insertOrUpdate(userId, chargedPoint);

        // 포인트 충전 이력 남기기
        pointHistoryTable.insert(userId, point, CHARGE, System.currentTimeMillis());

        return newUserPoint;
    }

    public UserPoint usePoint(final long userId, final long point) throws Exception {
        final UserPoint foundUserPoint = userPointTable.selectById(userId);

        final long usePoint = foundUserPoint.point() - point;
        if (usePoint < 0) {
            // 잔고가 부족 하므로 예외 throw..
            throw new Exception("잔고가 부족 합니다.");
        }

        final UserPoint userPoint = userPointTable.insertOrUpdate(userId, usePoint);

        pointHistoryTable.insert(userPoint.id(), point, USE, System.currentTimeMillis());

        return userPoint;
    }

}
