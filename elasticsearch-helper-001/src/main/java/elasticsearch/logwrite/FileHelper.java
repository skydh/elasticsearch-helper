package elasticsearch.logwrite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import elasticsearch.utils.DealDataUtils;
import elasticsearch.utils.SerializableUtils;
import elasticsearch.vo.EsTypeVO;

/**
 * 用来帮助失败信息持久化到文件里面 该类对象交给spring托管吧
 * 
 * @author Lenovo
 *
 */
@Component
public class FileHelper {
	/**
	 * 持久化文件路径
	 */
	@Value("${elasticsearch.logfile}")
	private String path;
	/**
	 * 保留最近几个月的文件
	 */
	@Value("${elasticsearch.months}")
	private String months;
	@Autowired
	private DealDataUtils dealData;

	/**
	 * 写文件
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void logWirte(Object ret, int type) {
		try {
			File file = createLogFile(0);
			EsTypeVO vo = new EsTypeVO();
			vo.setObj(ret);
			vo.setTpye(type);
			String voString = SerializableUtils.objectSerialiable(vo);
			RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.write(voString.getBytes());
			randomFile.writeChars("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 额外处理
	 */
	@SuppressWarnings("resource")
	public void logRead() {
		try {
			File file = createLogFile(-1);
			RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
			String line = "";
			int times = 0;
			long lastSeek = 0L;
			while ((line = randomFile.readLine()) != null) {
				times++;
				try {
					dealData.distinguish((EsTypeVO) SerializableUtils.objectDeserialization(line));
					times = 0;
				} catch (ExecutionException e) {
					/**
					 * 这个异常是更新不存在的数据导致的，按照逻辑，list的时候已删除，不处理，符合顺序执行的原则
					 */
					e.printStackTrace();
				} catch (Exception e) {
					/**
					 * 重试3次后依旧无法发送数据，那么停止，把后面的所有数据全部打到新的日志里面。
					 */
					if (times > 3)
						break;

				}
				lastSeek = randomFile.getFilePointer();
			}
			if (lastSeek == randomFile.length()) {
				randomFile.close();
			} else {
				/**
				 * 打入到新的日志里面，日志过大，需要手动管锥处理
				 * 
				 */
				File newFile = createLogFile(-1);
				RandomAccessFile newRandomFile = new RandomAccessFile(newFile, "rw");
				byte[] b = new byte[20];
				int len = 0;
				while ((len = randomFile.read(b)) != -1) {
					newRandomFile.write(b, 0, len);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	public File createLogFile(int cursor) throws IOException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, cursor);
		File file = new File(path + "/" + cal.get(cal.YEAR) + "_" + (cal.get(cal.MONTH) + 1) + "_" + cal.get(cal.DATE));
		File fileParent = file.getParentFile();
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}
		if (!file.exists())
			file.createNewFile();

		return file;

	}

	/**
	 * 删除过期文件
	 * 
	 * @throws ParseException
	 */
	@SuppressWarnings("static-access")
	public void deleteExpireFiles() throws ParseException {
		int monthsInt = Integer.parseInt(months);
		Calendar cal = Calendar.getInstance();
		if (cal.get(cal.DATE) == 1) {
			cal.add(Calendar.MONTH, -monthsInt);
			long timeNode = cal.getTimeInMillis();
			File baseFile = new File(path);
			File[] files = baseFile.listFiles();
			for (File file : files) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
				Date bt = sdf.parse(file.getName());
				if (bt.getTime() < timeNode)
					file.delete();

			}
		}

	}

}
