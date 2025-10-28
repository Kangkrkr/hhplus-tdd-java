package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.IllegalPointException;
import io.hhplus.tdd.exception.InsufficientPointException;
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

    public synchronized UserPoint chargePoint(final long userId, final long pointToCharge) {

        // 충전 대상을 조회
        final UserPoint foundUserPoint = userPointTable.selectById(userId);
        // 기존 포인트에 충전 포인트를 합산..
        final Point chargedPoint = foundUserPoint.point().charge(pointToCharge);
        // 데이터 업데이트
        final UserPoint newUserPoint = userPointTable.insertOrUpdate(userId, chargedPoint);
        // 포인트 충전 이력 남기기
        pointHistoryTable.insert(userId, pointToCharge, CHARGE, System.currentTimeMillis());

        return newUserPoint;
    }

    public synchronized UserPoint usePoint(final long userId, final long pointToUse) {
        final UserPoint foundUserPoint = userPointTable.selectById(userId);
        final Point leftPoint = foundUserPoint.point().use(pointToUse);
        final UserPoint userPoint = userPointTable.insertOrUpdate(userId, leftPoint);

        pointHistoryTable.insert(userPoint.id(), pointToUse, USE, System.currentTimeMillis());

        return userPoint;
    }

}
