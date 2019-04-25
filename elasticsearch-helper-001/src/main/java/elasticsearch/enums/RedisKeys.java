package elasticsearch.enums;

/**
 * 枚举一些redis的key
 * 
 * @author Lenovo
 *
 */
public enum RedisKeys {

	ESLISTKEY("es:list");

	
	
	
	public String key;
	RedisKeys(String key) {
		this.key = key;
	}
	

}
