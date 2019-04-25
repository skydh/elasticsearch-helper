package elasticsearch.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 该类主要用于序列化存到redis或者文档的类
 * 
 * @author Lenovo
 *
 */
@SuppressWarnings("serial")
@Data
public class EsTypeVO implements Serializable {

	/**
	 * 1:新增 2:修改 3:删除
	 */
	private int tpye;
	private Object obj;

}
