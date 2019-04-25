package elasticsearch.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import elasticsearch.exception.EsException;
import elasticsearch.utils.EsClassUtils;

/**
 * 删除方法切面。 约定大于配置，我们要求被删除注解的方法，传递的参数必须是对应vo,我们后置删除,第一个参数必须是对应vo
 * 
 * 目前这个版本基于数据回滚方式，利用spring事务回滚。
 * 
 * 测试如下： 节点断开。报错，获取异常信息，同时抛出一个运行异常，让mysql回滚
 * 
 * 节点无数据。情况不存在。已保证强一致性。
 * 
 * 失败直接回滚
 * 
 * @author Lenovo
 *
 */
@Aspect
@Component
public class DeleteAspect {

	@Autowired
	private TransportClient client;

	/**
	 * 定义切入点，切入点为EsDelete
	 */
	@Pointcut("@annotation(elasticsearch.annotation.EsDelete)")
	public void EsDelete() {
	}

	/**
	 * 后置通知：在连接点之前执行的通知
	 * 
	 * @param joinPoint
	 * @throws Throwable
	 */
	@AfterReturning(returning = "ret", pointcut = "EsDelete()")
	public void doAfterReturning(Object ret) {

		try {
			String EsId = EsClassUtils.getEsId(ret);
			String Index = EsClassUtils.getEsIndex(ret);
			client.prepareDelete(Index, Index, EsId).get();
		} catch (Exception e) {
			throw new EsException("es删除数据失败");
		}

	}

}
