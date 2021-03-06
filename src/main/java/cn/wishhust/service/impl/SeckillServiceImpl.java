package cn.wishhust.service.impl;

import cn.wishhust.dao.SeckillDao;
import cn.wishhust.dao.SuccessKilledDao;
import cn.wishhust.dao.cache.RedisDao;
import cn.wishhust.dto.Exposer;
import cn.wishhust.dto.SeckillExecution;
import cn.wishhust.entity.Seckill;
import cn.wishhust.entity.SuccessKilled;
import cn.wishhust.enums.SeckillStatEnum;
import cn.wishhust.exception.RepeatKillException;
import cn.wishhust.exception.SeckillCloseException;
import cn.wishhust.exception.SeckillException;
import cn.wishhust.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    // md5盐值，用于混淆md5
    private final String slat = "fgdgfdg^*(&^*lknkdsnadfl^(&^&9&";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getSeckillById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        // 优化点：缓存优化,超时基础上维护一致性
        // 1.访问redis
        // 查找秒杀产品
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            seckill = seckillDao.queryById(seckillId);
            if (null == seckill) {
                return new Exposer(false, seckillId);
            } else {
                redisDao.putSeckill(seckill);
            }
        }

        // 秒杀产品是否开启秒杀
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(),
                    startTime.getTime(), endTime.getTime());
        }

        // 秒杀开启，返回秒杀商品的id、用给接口加密的md5
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId+"/"+slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 使用注解控制事务方法的优点
     * 1. 开发团队达成一致约定，明确标注事务方法的编程风格
     * 2. 保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务外部
     * 3. 不是所有的方法都需要事务，如果只有一条修改操作，只读操作不需要事务控制
     *
     *
     * 注解@Transactional的方式，注解可以在方法定义、接口定义、类定义、public方法上，但是不能注解在private、final、static等方法上，因为Spring的事务管理默认是使用Cglib动态代理的：
     * private方法因为访问权限限制，无法被子类覆盖
     * final方法无法被子类覆盖
     * static是类级别的方法，无法被子类覆盖
     * protected方法可以被子类覆盖，因此可以被动态字节码增强
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        // 执行秒杀逻辑，减库存 + 记录购买行为
        try {
//            final int updateCount = seckillDao.reduceNumber(seckillId, new Date());
//            if (updateCount <= 0) {
//                // 没有更新到记录，秒杀结束
//                throw new SeckillCloseException("seckill is closed");
//            } else {
//                // 记录购买行为
//                final int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
//                if (insertCount <= 0) {
//                    // 重复秒杀
//                    throw new RepeatKillException("seckill repeated");
//                } else {
//                    // 秒杀成功
//                    final SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
//                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
//                }
//            }
            // 记录购买行为
            final int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                // 重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                // 减少库存，热点商品竞争
                final int updateCount = seckillDao.reduceNumber(seckillId, new Date());
                if (updateCount <= 0) {
                    // 没有更新到记录，秒杀结束，rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    // 秒杀成功，commit
                    final SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // Java异常分编译期异常和运行期异常，运行期异常不需要手工try-catch，Spring的的声明式事务只接收运行期异常回滚策略，非运行期异常不会帮我们回滚。
            // 编译时异常： 程序正确，但因为外在的环境条件不满足引发。
            // 运行期异常： 这意味着程序存在bug，如数组越界，0被除, 这类异常需要更改程序来避免，Java编译器强制要求处理这类异常。
            // 编译器只是进行语法的分析，分析出来的错误也只是语法上的错误，而运行期在真正在分配内存
            // Spring的事务默认是发生了RuntimeException才会回滚，发生了其他异常不会回滚
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillByProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();

        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        try {
            seckillDao.killByProcedure(map);
            final Integer result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, sk);
            } else {
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }
    }
}
