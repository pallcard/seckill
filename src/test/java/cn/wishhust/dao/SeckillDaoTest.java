package cn.wishhust.dao;

import cn.wishhust.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.swing.text.StyledEditorKit;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置spring和junit整合，junit启动时加载springIOC容器
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    @Resource
    private SeckillDao seckillDao;

    @Test
    public void reduceNumber() {
        final int i = seckillDao.reduceNumber(1000, new Date());
        System.out.println(i);
    }

    @Test
    public void queryById() {
        long id = 1000;
        final Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void queryAll() {
        // java 没有保存形参记录
        // @Param("offset")
        final List<Seckill> seckills = seckillDao.queryAll(0, 100);
        for (Seckill s : seckills) {
            System.out.println(s);
        }
    }
}
