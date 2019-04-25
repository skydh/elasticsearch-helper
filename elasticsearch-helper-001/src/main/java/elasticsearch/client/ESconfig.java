package elasticsearch.client;

import java.net.InetAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Title: ESconfig
 * @Description:
 * @author: dh
 */
@Configuration
public class ESconfig {

	/**
	 * elk集群地址
	 */
	@Value("${elasticsearch.ip}")
	private String hostName;
	/**
	 * 端口
	 */
	@Value("${elasticsearch.port}")
	private String port;
	/**
	 * 集群名称
	 */
	@Value("${elasticsearch.cluster.name}")
	private String clusterName;

	/**
	 * 连接池
	 */
	@Value("${elasticsearch.pool}")
	private String poolSize;

	@Bean
	public TransportClient getClient() {

		TransportClient transportClient = null;
		try {
			// 配置信息
			Settings esSetting = Settings.builder().put("client.transport.sniff", true)// 增加嗅探机制，找到ES集群

					.put("cluster.name", clusterName).build();
			// 配置信息Settings自定义,下面设置为EMPTY
			transportClient = new PreBuiltTransportClient(esSetting);
			TransportAddress transportAddress = new TransportAddress(InetAddress.getByName(hostName),
					Integer.valueOf(port));
			transportClient.addTransportAddresses(transportAddress);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return transportClient;
	}
}
