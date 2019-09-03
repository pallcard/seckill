package cn.wishhust.service;


import cn.wishhust.dto.Exposer;
import cn.wishhust.dto.SeckillExecution;
import cn.wishhust.entity.Seckill;
import cn.wishhust.exception.RepeatKillException;
import cn.wishhust.exception.SeckillCloseException;
import cn.wishhust.exception.SeckillException;

import java.util.List;

/**
 * 业务接口，我们应该站在“使用者”角度设计接口
 * 三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 * 方法定义粒度：功能明确而且单一。
 * 参数：方法所需要的数据，供使用者传入，明确方法所需要的数据，而且尽可能友好，简练。
 * 返回值：一般情况下，entity数据不够，需要自定义DTO,也有可能抛出异常，需要自定义异常，不管是DTO还是异常，尽可能将接口调用的信息返回给使用者，哪怕是失败信息。
 */
public interface SeckillService {

    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getSeckillById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
        throws SeckillException, RepeatKillException, SeckillCloseException;

    /**
     * 执行秒杀操作 By 存储过程
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    SeckillExecution executeSeckillByProcedure(long seckillId, long userPhone, String md5);

}
