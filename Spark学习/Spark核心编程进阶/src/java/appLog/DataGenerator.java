package cn.spark.study.core.upgrade.applog;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
/*
 * 模拟生成数据
 */
public class DataGenerator {
	
	public static void main(String[] args) throws Exception {
		Random random = new Random();
		
		// 生成100个deviceID
		List<String> deviceIDs = new ArrayList<String>();
		for(int i = 0; i < 100; i++) {
			deviceIDs.add(getRandomUUID());
		}
		
		StringBuffer buffer = new StringBuffer("");  
		
		for(int i = 0; i < 1000; i++) {
			// 生成随机时间戳
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());    
			cal.add(Calendar.MINUTE, -random.nextInt(600)); 
			long timestamp = cal.getTime().getTime();
		
			// 生成随机deviceID
			String deviceID = deviceIDs.get(random.nextInt(100));  
			
			// 生成随机的上行流量
			long upTraffic = random.nextInt(100000);
			// 生成随机的下行流量
			long downTraffic = random.nextInt(100000);
			
			buffer.append(timestamp).append("\t")  
					.append(deviceID).append("\t")  
					.append(upTraffic).append("\t")
					.append(downTraffic).append("\n");  
		}
		
		PrintWriter pw = null;  
		try {
			pw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream("C:\\Users\\Administrator\\Desktop\\access.log")));
			pw.write(buffer.toString());  
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}
	}
	
	private static String getRandomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
}
