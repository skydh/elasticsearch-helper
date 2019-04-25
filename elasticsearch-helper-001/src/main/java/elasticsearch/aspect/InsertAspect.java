package elasticsearch.aspect;

import java.io.IOException;

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
 * 新增方法切面。我们后置删除，需要返回对应实体类。
 * 
 * 该方案是数据添加到日志的方式 添加数据失败， 只可能网络抖动导致，其他因素不予考虑 因此要么成功，要么失败
 * 失败则是把数据加入到list里面。同时顺序放到本地日志里面。 凌晨12点执行。
 * 
 * 
 * @author Lenovo
 *
 */
@Aspect
@Component
public class InsertAspect {
	@Autowired
	private DealDataUtils dealData;
	@Autowired
	private RedisUtils redis;
	@Autowired
	private FileHelper fileHelper;

	/**
	 * 定义切入点，切入点为EsAdd
	 */
	@Pointcut("@annotation(elasticsearch.annotation.EsAdd)")
	public void EsAdd() {
	}

	@AfterReturning(returning = "ret", pointcut = "EsAdd()")
	public void doAfterReturning(Object ret) {
		try {
			dealData.doInsert(ret);
		} catch (NoNodeAvailableException e) {
			System.out.println("找不到节点，服务挂了，或者网络抖动导致数据无法到达");
			fileHelper.logWirte(ret, 1);
			redis.putToList(ret, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
