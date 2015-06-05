package snails.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

/**
 * @ClassName: RedisUtils
 * @Description: redis 存与取
 * @author chenlinlin
 * @date 2015年3月12日 上午12:24:59
 */
public class RedisUtils {

	private static Logger log = LoggerFactory.getLogger(RedisUtils.class);

	public static boolean persistVeryCode(String phoneNumber, String code) {

		boolean flag = false;
		Jedis jedis = RedisPool.getJedis();
		try {
			jedis.set(phoneNumber, code);
			jedis.expire(phoneNumber, 60);
			flag = true;
		} catch (Exception e) {
			log.error("persistVeryCode error", e);
		} finally {
			RedisPool.returnResource(jedis);
		}
		return flag;
	}

	public static String fetchCode(String phoneNumber) {
		String result = "nil";
		Jedis jedis = RedisPool.getJedis();
		try {
			result = jedis.get(phoneNumber);
		} catch (Exception e) {
			log.error("fetchCode error", e);
		} finally {
			RedisPool.returnResource(jedis);
		}
		return result;
	}

	public static void incrCount(Long activityId) {
		Jedis jedis = RedisPool.getJedis();
		try {
			jedis.incr(String.valueOf(activityId));
		} catch (Exception e) {
			log.error("fetchCode error", e);
		} finally {
			RedisPool.returnResource(jedis);
		}
	}

	public static void main(String args[]) {
		System.out.println(RedisUtils.fetchCode("18768125168"));
	}
}
