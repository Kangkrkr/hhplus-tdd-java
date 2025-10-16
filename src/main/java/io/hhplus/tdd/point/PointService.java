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

    public UserPoint chargePoint(final long userId, final long pointToCharge) throws Exception {
        // 잘못된 포인트가 인자로 넘어 온 경우 예외를 통한 early return ..
        if (pointToCharge <= 0) {
            throw new Exception("충전할 포인트는 0 이상이어야 합니다.");
        }

        // 충전 대상을 조회
        final UserPoint foundUserPoint = userPointTable.selectById(userId);

        // 기존 포인트에 충전 포인트를 합산..
        final long chargedPoint = foundUserPoint.point() + pointToCharge;
        // 데이터 업데이트
        final UserPoint newUserPoint = userPointTable.insertOrUpdate(userId, chargedPoint);

        // 포인트 충전 이력 남기기
        pointHistoryTable.insert(userId, pointToCharge, CHARGE, System.currentTimeMillis());

        return newUserPoint;
    }

    public UserPoint usePoint(final long userId, final long pointToUse) throws Exception {
        // 잘못된 포인트가 인자로 넘어 온 경우 예외를 통한 early return ..
        if (pointToUse <= 0) {
            throw new Exception("사용할 포인트는 0 이상이어야 합니다.");
        }

        final UserPoint foundUserPoint = userPointTable.selectById(userId);
        final long leftPoint = foundUserPoint.point() - pointToUse;
        if (leftPoint < 0) {
            // 잔고가 부족 하므로 예외 throw..
            // 나중에 커스텀 예외를 만들어서 처리 하면 좋을 것 같다.
            throw new Exception("잔고가 부족 합니다.");
        }

        final UserPoint userPoint = userPointTable.insertOrUpdate(userId, leftPoint);

        pointHistoryTable.insert(userPoint.id(), pointToUse, USE, System.currentTimeMillis());

        return userPoint;
    }

}
