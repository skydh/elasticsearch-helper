package elasticsearch.Consumer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import elasticsearch.utils.DealDataUtils;
import elasticsearch.utils.RedisUtils;
import elasticsearch.utils.SerializableUtils;
import elasticsearch.vo.EsTypeVO;

/**
 * 启动一个消费者来消费这个队列消息
 * 
 * @author Lenovo
 *
 */
@Component
@Order(value = 3)
public class EsConsumer implements CommandLineRunner {
	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private DealDataUtils dealData;

	@Override
	public void run(String... args) throws Exception {
		new Thread(() -> {
			while (true) {
				try {
					getMessage();
				} catch (RedisConnectionFailureException e) {
					try {
						/**
						 * 若是连接不到redis，睡眠一秒后重新启动
						 */
						Thread.sleep(2000);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}).start();

	}

	public void getMessage() {
		while (true) {
			List<byte[]> listByte = redisUtils.getData();
			if (listByte != null) {
				/**
				 * 第一个是队列的值，第二个元素才是具体的值，因为这个允许多个队列
				 */
				try {
					dealData.distinguish(
							(EsTypeVO) SerializableUtils.objectDeserialization(new String(listByte.get(1))));
				} catch (IOException | InterruptedException | ExecutionException e) {
					/**
					 * redis队列消费有问题，不处理了
					 */
					e.printStackTrace();
				}
			}
		}

	}

}
