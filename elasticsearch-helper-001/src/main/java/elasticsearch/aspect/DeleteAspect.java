package elasticsearch.aspect;

import java.io.IOException;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import elasticsearch.logwrite.FileHelper;
import elasticsearch.utils.DealDataUtils;
import elasticsearch.utils.RedisUtils;

/**
 * 删除方法切面。 约定大于配置，我们要求被删除注解的方法，传递的参数必须是对应vo,我们前置删除,第一个参数必须是对应vo
 * 
 * 网络抖动，加入到list,若是无法找到 ，可能数据还没有刷到es，同样加入到list，顺序执行即可。当然这些数据加入到本地日志，凌晨来保证其最终一致性。
 * 
 * 
 * 
 * @author Lenovo
 *
 */
@Aspect
@Component
public class DeleteAspect {

	@Autowired
	private DealDataUtils dealData;
	@Autowired
	private RedisUtils redis;
	@Autowired
	private FileHelper fileHelper;

	/**
	 * 定义切入点，切入点为EsDelete
	 */
	@Pointcut("@annotation(elasticsearch.annotation.EsDelete)")
	public void EsDelete() {
	}

	/**
	 * 后置通知：在连接点之前执行的通知 NOT_FOUND
	 * 
	 * @param joinPoint
	 * @throws IOException
	 * @throws Throwable
	 */
	@AfterReturning(returning = "ret", pointcut = "EsDelete()")
	public void doAfterReturning(Object ret) throws IOException {
		try {
			DeleteResponse result = dealData.doDelet(ret);
			if (result.getResult().toString().equals("NOT_FOUND")) {
				System.out.println("删除数据不存在");
				fileHelper.logWirte(ret, 3);
				redis.putToList(ret, 3);
			}
		} catch (NoNodeAvailableException e) {
			System.out.println("找不到节点，服务挂了，或者网络抖动导致数据无法到达");
			fileHelper.logWirte(ret, 3);
			redis.putToList(ret, 3);
		}
	}

}
