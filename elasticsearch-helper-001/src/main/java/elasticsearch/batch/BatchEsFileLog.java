package elasticsearch.batch;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import elasticsearch.logwrite.FileHelper;

/**
 * 
 * 定时器，半夜12点准时刷信息到es。 同时每月1号删除前前1月的文档。
 */
@Component
public class BatchEsFileLog {

	@Autowired
	private FileHelper fileHelper;

	/**
	 * 每天凌晨 12分进行数据刷新
	 */
	@Scheduled(cron = "0 0 12 * * ?")
	private void proces() {
		try {
			/**
			 * 将日志数据重新导入
			 */
			fileHelper.logRead();
			/**
			 * 删除过期文件
			 */
			fileHelper.deleteExpireFiles();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
