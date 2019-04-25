package elasticsearch.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.action.update.UpdateRequest;
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
 * 更新方法切面。 将来可能存在差异，因此和add先分开，保证隔离性。 后置拦截，
 * 
 * * 目前没有加失败重来处理，后续加
 * 
 * 更新不存在的数据，抛出异常。
 * 
 * @author Lenovo
 *
 */
@Aspect
@Component
public class UpdateAspect {

	@Autowired
	private TransportClient client;

	/**
	 * 定义切入点，切入点为EsUpdate
	 */
	@Pointcut("@annotation(elasticsearch.annotation.EsUpdate)")
	public void EsUpdate() {
	}

	/**
	 * 后置通知：在连接点之前执行的通知
	 * 
	 * @param joinPoint
	 * @throws Throwable
	 */
	@AfterReturning(returning = "ret", pointcut = "EsUpdate()")
	public void doAfterReturning(Object ret) {
		try {
			EsClassVO classVO = EsClassUtils.getEsAll(ret);
			String Index = EsClassUtils.getEsIndex(ret);
			XContentBuilder content = XContentFactory.jsonBuilder().startObject();
			EsAssembleUtils.getXContentBuilder(content, classVO.getMap());
			UpdateRequest updateRequest = new UpdateRequest(Index, Index, classVO.getEsId()).doc(content);
			client.update(updateRequest).get();
		} catch (Exception e) {
			throw new EsException("es更新数据失败");

		}

	}
}
