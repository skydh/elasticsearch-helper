package elasticsearch.vo;

import java.util.HashMap;

import lombok.Data;

/**
 * 需要加到es的属性和esID
 * 
 * @author Lenovo
 *
 */
@Data
public class EsClassVO {
	private HashMap<String, String> map;
	private String esId;

	

}
