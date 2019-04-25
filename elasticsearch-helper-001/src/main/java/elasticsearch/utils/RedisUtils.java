package elasticsearch.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import elasticsearch.enums.RedisKeys;
import elasticsearch.enums.TimeEnum;
import elasticsearch.vo.EsTypeVO;

/**
 * redis list 封装
 * 
 * @author Lenovo
 *
 */
@Component
public class RedisUtils {
	@Autowired
	private StringRedisTemplate redis;

	public void putToList(Object vo, int type) {
		EsTypeVO etvo = new EsTypeVO();
		etvo.setObj(vo);
		etvo.setTpye(type);
		String dataString = SerializableUtils.objectSerialiable(etvo);
		redis.opsForList().leftPush(RedisKeys.ESLISTKEY.key, dataString);
	}

	public List<byte[]> getData() {
		RedisConnection connection = redis.getConnectionFactory().getConnection();
		return connection.bRPop(TimeEnum.BLOCKTIME.key, RedisKeys.ESLISTKEY.key.getBytes());

	}

}
