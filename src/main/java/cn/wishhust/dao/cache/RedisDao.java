package cn.wishhust.dao.cache;

import cn.wishhust.entity.Seckill;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(RedisDao.class);

    private final JedisPool jedisPool;

    public RedisDao(String host, int port) {
        this.jedisPool = new JedisPool(host, port);
    }

    public RedisDao(String host, int port, int timeout, String password) {
        this.jedisPool = new JedisPool(new GenericObjectPoolConfig(), host, port, timeout, password);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId) {
        try {
            final Jedis jedis = jedisPool.getResource();

            try {
                String key = "seckill:" + seckillId;
                // get -> byte[] -> 反序列化 -> object(Seckill)
                final byte[] bytes = jedis.get(key.getBytes());
                // 缓存里取得
                if (bytes != null) {
                    // 空对象
                    final Seckill seckill = schema.newMessage();
                    // seckill 被反序列化
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill) {
        // object(Seckill) -> 序列化 -> byte[]

        try {
            final Jedis jedis = jedisPool.getResource();
            String key = "seckill:" + seckill.getSeckillId();
            final byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            int timeout = 60 * 60;
            String result = jedis.setex(key.getBytes(),timeout,bytes);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
