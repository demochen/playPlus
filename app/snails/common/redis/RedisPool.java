package snails.common.redis;

import play.Play;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisPool {

	// Redis ServerIP
	// private static String ADDR = "121.40.177.178";
	private static String ADDR = Play.configuration.getProperty("redis.server.url", "121.40.177.178");

	// Redis　port
	private static int PORT = 6379;

	// Redis 密码
	private static String AUTH = Play.configuration.getProperty("redis.server.password", "foobared");
	// private static String AUTH = "foobared";
	private static int MAX_ACTIVE = 1024;

	private static int MAX_IDLE = 200;

	private static int MAX_WAIT = 10000;

	private static int TIMEOUT = 10000;

	private static boolean TEST_ON_BORROW = true;

	private static JedisPool jedisPool = null;

	static {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxActive(MAX_ACTIVE);
			config.setMaxIdle(MAX_IDLE);
			config.setMaxWait(MAX_WAIT);
			config.setTestOnBorrow(TEST_ON_BORROW);
			jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT, AUTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static Jedis getJedis() {
		try {
			if (jedisPool != null) {
				Jedis resource = jedisPool.getResource();
				return resource;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void returnResource(final Jedis jedis) {
		if (jedis != null) {
			jedisPool.returnResource(jedis);
		}
	}
}
