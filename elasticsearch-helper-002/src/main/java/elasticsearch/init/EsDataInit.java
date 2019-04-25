package elasticsearch.init;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import elasticsearch.annotation.EsClass;
import elasticsearch.vo.EsAggVO;

/**
 * 数据初始化类
 * 
 * 目前没有加失败重来处理，后续加
 * 
 * @author Lenovo
 *
 */
@Component
@Order(value = 2)
public class EsDataInit implements CommandLineRunner {

	/**
	 * 包扫描路径
	 */
	@Value("${elasticsearch.scanPackage}")
	private String scanPackage;

	/**
	 * 是否初始化数据
	 */
	@Value("${elasticsearch.isScan}")
	private String isSacn;

	/**
	 * 包扫描路径
	 */
	@Value("${elasticsearch.limitSize}")
	private String limitSize;

	@Autowired
	private TransportClient client;

	@Resource
	private JdbcTemplate jdbcTemplate;

	@SuppressWarnings("rawtypes")
	@Override
	public void run(String... var1) throws Exception {
		if ("yes".equals(isSacn)) {

			Set<BeanDefinition> beanDefinitionSet = getEsClass();
			Iterator<BeanDefinition> it = beanDefinitionSet.iterator();
			Integer size = Integer.parseInt(limitSize);
			while (it.hasNext()) {
				int startLimit = 0;
				boolean isBreak = true;
				
				BeanDefinition str = it.next();
				String className = str.getBeanClassName();
				Class cla = Class.forName(className);
				EsAggVO esAggVO = EsFieldRelationUtils.getRelation(cla);
				do {
					StringBuilder sqlSb = new StringBuilder(esAggVO.getSql());
					sqlSb.append("  limit " + startLimit * size + "," + size + ";");
					List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlSb.toString());
					if (list.size() > 0) {
						BulkRequestBuilder bulkRequest = client.prepareBulk();
						batchInsert(bulkRequest, esAggVO, list);
						bulkRequest.execute().actionGet();
						startLimit++;
					} else {
						isBreak = false;
					}
				} while (isBreak);
			}

		}
	}

	/**
	 * 批量插入es数据
	 * 
	 * @throws IOException
	 */
	public void batchInsert(BulkRequestBuilder bulkRequest, EsAggVO esAggVO, List<Map<String, Object>> list)
			throws IOException {
		HashMap<String, String> fieldMapping = esAggVO.getMap();
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> map = list.get(i);
			IndexRequestBuilder indexRequestBuilder = client.prepareIndex(esAggVO.getIndexName(),
					esAggVO.getIndexName(), map.get(esAggVO.getTableId()).toString());
			XContentBuilder xContentBuilder = jsonBuilder().startObject();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				/**
				 * 排除id
				 */
				if (!entry.getKey().equals(esAggVO.getTableId())) {
					xContentBuilder.field(fieldMapping.get(entry.getKey()), entry.getValue());
				}
			}
			xContentBuilder.endObject();
			indexRequestBuilder.setSource(xContentBuilder);
			bulkRequest.add(indexRequestBuilder);
		}
	}

	/**
	 * 获取需要加载的类
	 * 
	 * @return
	 */
	public Set<BeanDefinition> getEsClass() {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false); // 不使用默认的TypeFilter
		provider.addIncludeFilter(new AnnotationTypeFilter(EsClass.class));
		return provider.findCandidateComponents(scanPackage);

	}

}
