package elasticsearch.enums;

/**
 * 
 * @author Lenovo
 *
 */
public enum TimeEnum {
	/**
	 * es坏消息 阻塞时间
	 */
	BLOCKTIME(60),
	/**
	 * 消费者重新连接睡眠时间
	 * 
	 */
	SLEEPTIME(2000);

	public int key;

	TimeEnum(int key) {
		this.key = key;
	}

}
