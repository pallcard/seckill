package cn.wishhust.dao;

import cn.wishhust.entity.SuccessKilled;

public interface SuccessKilledDao {
    /**
     * 插入购买明细，可过滤重复（联合主键）
     * @param seckillId
     * @param userPhone
     * @return 插入结果的数量
     */
    int insertSuccessKilled(long seckillId, long userPhone);

    /**
     * 根据id查询SuccessKilled
     * @param seckillId
     * @param userPhone
     * @return
     */
    SuccessKilled queryByIdWithSeckill(long seckillId, long userPhone);
}
