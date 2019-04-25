package elasticsearch.vo;

import java.util.HashMap;

import lombok.Data;

/**
 * es filed-mysql field EsInde, mysqlTableName
 * 
 * @author Lenovo
 *
 */
@Data
public class EsAggVO {

	private HashMap<String, String> map;

	private String IndexName;

	private String tableName;

	private String tableId;

	private String sql;

}
