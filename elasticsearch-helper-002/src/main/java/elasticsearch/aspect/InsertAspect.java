package elasticsearch.aspect;

import java.io.IOException;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import elasticsearch.exception.EsException;
import elasticsearch.utils.EsAssembleUtils;
import elasticsearch.utils.EsClassUtils;
import elasticsearch.vo.EsClassVO;

/**
 * 新增方法切面。我们后置删除，需要返回对应实体类。
 * 
 * * 目前没有加失败重来处理，后续加
 * 
 * @author Lenovo
 *
 */
@Aspect
@Component
public class InsertAspect {

	@Autowired
	private TransportClient client;

	/**
	 * 定义切入点，切入点为EsAdd
	 */
	@Pointcut("@annotation(elasticsearch.annotation.EsAdd)")
	public void EsAdd() {
	}

	@AfterReturning(returning = "ret", pointcut = "EsAdd()")
	public void doAfterReturning(Object ret) {
		EsClassVO classVO = EsClassUtils.getEsAll(ret);
		String Index = EsClassUtils.getEsIndex(ret);
		try {
			XContentBuilder content = XContentFactory.jsonBuilder().startObject();
			EsAssembleUtils.getXContentBuilder(content, classVO.getMap());
			client.prepareIndex(Index, Index, classVO.getEsId()).setSource(content).get();
		} catch (IOException e) {
			throw new EsException("es添加数据失败");

		}

	}

}
