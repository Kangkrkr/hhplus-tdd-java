package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public UserPoint findUserPointByUserId(long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> findAllPointHistoryByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
    
}
