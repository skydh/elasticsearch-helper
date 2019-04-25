package elasticsearch.utils;

import java.lang.reflect.Field;
import java.util.HashMap;

import elasticsearch.annotation.EsClass;
import elasticsearch.annotation.EsField;
import elasticsearch.annotation.EsId;
import elasticsearch.vo.EsClassVO;

/**
 * 处理aop获取到的节点的数据
 * 
 * @author Lenovo
 *
 */
public class EsClassUtils {

	/**
	 * 获取es的Index
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String getEsIndex(Object obj) {
		Class test = obj.getClass();
		EsClass esClass = (EsClass) test.getAnnotation(EsClass.class);
		return esClass.esIndex();
	}

	/**
	 * 获取需要加到es的field和value
	 * 
	 * @param obj
	 * @return
	 */
	public static HashMap<String, String> getEsFieldAndValue(Object obj) {
		HashMap<String, String> map = new HashMap<String, String>();
		Field[] fields = obj.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			/**
			 * 若是加了EsField。则是需要加入到es的属性
			 */
			EsField esField = fields[i].getAnnotation(EsField.class);
			if (esField != null) {
				try {
					map.put(esField.esField(), fields[i].get(obj).toString());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	/**
	 * 获取需要加到esid,以及es的field和value
	 * 
	 * @param obj
	 * @return
	 */
	public static EsClassVO getEsAll(Object obj) {
		EsClassVO claaVO = new EsClassVO();
		HashMap<String, String> map = new HashMap<String, String>();
		Field[] fields = obj.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				EsId esld = fields[i].getAnnotation(EsId.class);
				EsField esField = fields[i].getAnnotation(EsField.class);
				fields[i].setAccessible(true);
				if (esld != null) {
					claaVO.setEsId(fields[i].get(obj).toString());
				}
				/**
				 * 空的字段就不要加进去了
				 */
				if (esField != null && fields[i].get(obj) != null) {
					map.put(esField.esField(), fields[i].get(obj).toString());
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		claaVO.setMap(map);
		return claaVO;
	}

	/**
	 * 获取需要加到esid,value.
	 * 
	 * @param obj
	 * @return
	 */
	public static String getEsId(Object obj) {
		Field[] fields = obj.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			/**
			 * 若是加了EsField。则是需要加入到es的属性
			 */
			EsId esld = fields[i].getAnnotation(EsId.class);
			if (esld != null) {
				try {
					fields[i].setAccessible(true);

					return fields[i].get(obj).toString();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
