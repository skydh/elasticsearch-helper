package elasticsearch.utils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import elasticsearch.vo.EsClassVO;
import elasticsearch.vo.EsTypeVO;

/**
 * 在这里将一些方法提出来
 * 
 * @author Lenovo
 *
 */
@Component
public class DealDataUtils {
	@Autowired
	private TransportClient client;

	/**
	 * 删除操作
	 * 
	 * @param ret
	 * @return
	 */
	public DeleteResponse doDelet(Object ret) {
		String EsId = EsClassUtils.getEsId(ret);
		String Index = EsClassUtils.getEsIndex(ret);
		return client.prepareDelete(Index, Index, EsId).get();
	}

	/**
	 * 插入操作
	 * 
	 * @param ret
	 * @throws IOException
	 */
	public void doInsert(Object ret) throws IOException {
		EsClassVO classVO = EsClassUtils.getEsAll(ret);
		String Index = EsClassUtils.getEsIndex(ret);
		XContentBuilder content = XContentFactory.jsonBuilder().startObject();
		EsAssembleUtils.getXContentBuilder(content, classVO.getMap());
		client.prepareIndex(Index, Index, classVO.getEsId()).setSource(content).get();
	}

	/**
	 * 更新操作
	 * 
	 * @param ret
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void doUpdate(Object ret) throws InterruptedException, ExecutionException, IOException {
		EsClassVO classVO = EsClassUtils.getEsAll(ret);
		String Index = EsClassUtils.getEsIndex(ret);
		XContentBuilder content = XContentFactory.jsonBuilder().startObject();
		EsAssembleUtils.getXContentBuilder(content, classVO.getMap());
		UpdateRequest updateRequest = new UpdateRequest(Index, Index, classVO.getEsId()).doc(content);
		client.update(updateRequest).get();
	}

	/**
	 * 丢给其自行处理
	 * 
	 * @param vo
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void distinguish(EsTypeVO vo) throws IOException, InterruptedException, ExecutionException {

		if (vo.getTpye() == 1) {
			doInsert(vo.getObj());
		} else if (vo.getTpye() == 2) {
			doUpdate(vo.getObj());
		} else {
			doDelet(vo.getObj());
		}

	}

}
