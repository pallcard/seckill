package cn.wishhust.service;

import cn.wishhust.dto.Exposer;
import cn.wishhust.dto.SeckillExecution;
import cn.wishhust.entity.Seckill;
import cn.wishhust.exception.RepeatKillException;
import cn.wishhust.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        final List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}",seckillList);
    }

    @Test
    public void getSeckillById() {
        int seckillId = 1000;
        final Seckill seckill = seckillService.getSeckillById(seckillId);
        logger.info("seckill={}", seckill);

    }

//    @Test
//    public void exportSeckillUrl() {
//        int seckillId = 1002;
//        final Exposer exposer = seckillService.exportSeckillUrl(seckillId);
//        logger.info("exposer={}", exposer);
////        Exposer{exposed=true, md5='d38fbd85e78d7e92e3b508f79abb2588', seckillId=1002, now=0, start=0, end=0}
//    }
//
//    @Test
//    public void executeSeckill() {
//        int seckillId = 1002;
//        String md5 = "d38fbd85e78d7e92e3b508f79abb2588";
//        try {
//            final SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, 18162327111L, md5);
//            logger.info("seckillExecution={}", seckillExecution);
//        } catch (RepeatKillException e) {
//            logger.error(e.getMessage());
//        } catch (SeckillCloseException e) {
//            logger.error(e.getMessage());
//        }
//
//    }

    @Test
    public void seckillLogic() throws Exception {
        int seckillId = 1002;
        final Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            try {
                final SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, 18162327111L, md5);
                logger.info("seckillExecution={}", seckillExecution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.warn("exposer={}", exposer);
        }
    }
}
