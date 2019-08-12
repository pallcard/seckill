package cn.wishhust.dao;

import cn.wishhust.entity.SuccessKilled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() {
        final int i = successKilledDao.insertSuccessKilled(1001, 18162327111L);
        System.out.println(i);
    }

    @Test
    public void queryByIdWithSeckill() {
        final SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(1001, 18162327111L);
        System.out.println(successKilled);
    }
}
