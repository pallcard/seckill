package cn.wishhust.service.impl;

import cn.wishhust.dao.SeckillDao;
import cn.wishhust.dao.SuccessKilledDao;
import cn.wishhust.dto.Exposer;
import cn.wishhust.dto.SeckillExecution;
import cn.wishhust.entity.Seckill;
import cn.wishhust.entity.SuccessKilled;
import cn.wishhust.enums.SeckillStatEnum;
import cn.wishhust.exception.RepeatKillExecption;
import cn.wishhust.exception.SeckillCloseException;
import cn.wishhust.exception.SeckillException;
import cn.wishhust.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private SeckillDao seckillDao;

    private SuccessKilledDao successKilledDao;

    // md5盐值，用于混淆md5
    private final String slat = "";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getSeckillById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        final Seckill seckill = seckillDao.queryById(seckillId);
        if (null == seckill) {
            return new Exposer(false, seckillId);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId,nowTime.getTime(),
                    startTime.getTime(),endTime.getTime());
        }
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId+"/"+slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillExecption, SeckillCloseException {
        if (md5 == null || md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        try {
            final int updateCount = seckillDao.reduceNumber(seckillId, new Date());
            if (updateCount <= 0) {
                throw new SeckillCloseException("seckill is closed");
            } else {
                final int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                if (insertCount <= 0) {
                    // 重复秒杀
                    throw new RepeatKillExecption("seckill repeated");
                } else {
                    final SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);

                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillExecption e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }


    }
}
