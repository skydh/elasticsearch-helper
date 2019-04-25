package elasticsearch.init;

import java.lang.reflect.Field;
import java.util.HashMap;

import elasticsearch.annotation.EsClass;
import elasticsearch.annotation.EsField;
import elasticsearch.annotation.EsId;
import elasticsearch.vo.EsAggVO;

/**
 * es filed 和mysql field 对应工具类，主要用于数据初始化使用。
 * 
 * *
 * 
 * @author Lenovo
 *
 */
public class EsFieldRelationUtils {

	/**
	 * 获取对应关系
	 * 
	 * 作为项目初始化，里面的数据都是基本数据，不存在逻辑删除数据，若是存在，自行删除。 若是有强烈要求。可以加注解控制
	 * 
	 * @param cla
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static EsAggVO getRelation(Class cla) {
		EsAggVO vo = new EsAggVO();
		/**
		 * field table - es
		 */
		HashMap<String, String> map = new HashMap<String, String>();
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		EsClass esClass = (EsClass) cla.getAnnotation(EsClass.class);
		vo.setIndexName(esClass.esIndex());
		vo.setTableName(esClass.tableName());
		Field[] fields = cla.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			/**
			 * 若是加了EsField。则是需要加入到es的属性
			 */
			EsField esField = fields[i].getAnnotation(EsField.class);
			if (esField != null) {
				map.put(esField.mysqlField(), esField.esField());
				sql.append(esField.mysqlField() + ",");
			}
			EsId esId = fields[i].getAnnotation(EsId.class);
			if (esId != null) {
				sql.append(esId.mysqlld() + ",");
				vo.setTableId(esId.mysqlld());
			}
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(" from " + esClass.tableName());
		vo.setSql(sql.toString());
		vo.setMap(map);
		return vo;

	}

}
