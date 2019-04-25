package elasticsearch.aspect;

import java.util.concurrent.ExecutionException;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import elasticsearch.logwrite.FileHelper;
import elasticsearch.utils.DealDataUtils;
import elasticsearch.utils.RedisUtils;

/**
 * 更新方法切面。 将来可能存在差异，因此和add先分开，保证隔离性。 后置拦截，
 * 
 * 
 * 失败，加入到list 若是更新失败，比如可能是数据还没加入到es，也是顺序直接放到list里面去。同时顺序放到本地日志里面。
 *
 * 
 * 
 * @author Lenovo
 *
 */
@Aspect
@Component
public class UpdateAspect {

	@Autowired
	private DealDataUtils dealData;

	@Autowired
	private RedisUtils redis;
	@Autowired
	private FileHelper fileHelper;

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
			dealData.doUpdate(ret);
		} catch (ExecutionException e) {
			System.out.println("更新数据不存在");
			fileHelper.logWirte(ret, 2);
			redis.putToList(ret, 2);
		} catch (NoNodeAvailableException e) {
			System.out.println("找不到节点，服务挂了，或者网络抖动导致数据无法到达");
			fileHelper.logWirte(ret, 2);
			redis.putToList(ret, 2);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
}
