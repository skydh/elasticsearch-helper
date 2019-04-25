package elasticsearch.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * 拼装es属性
 * 
 * @author Lenovo
 *
 */
public class EsAssembleUtils {

	/**
	 * 拼装字段
	 * 
	 * @param obj
	 * @return
	 */
	public static XContentBuilder getXContentBuilder(XContentBuilder content, HashMap<String, String> map) {
		try {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				content.field(entry.getKey(), entry.getValue());
			}
			content.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
}
