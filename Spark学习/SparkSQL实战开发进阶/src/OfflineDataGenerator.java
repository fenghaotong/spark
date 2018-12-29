package cn.spark.study.sql.upgrade;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 离线数据生成器
 * @author Administrator
 *
 */
public class OfflineDataGenerator {
	
	public static void main(String[] args) throws Exception {
		StringBuffer buffer = new StringBuffer("");  
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Random random = new Random();
		String[] sections = new String[] {"country", "international", "sport", "entertainment", "movie", "carton", "tv-show", "technology", "internet", "car"};
		
		int[] newOldUserArr = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		
		// 生成日期，默认就是昨天
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());  
		cal.add(Calendar.DAY_OF_YEAR, -1);  
		Date yesterday = cal.getTime();
		
		String date = sdf.format(yesterday);
		
		// 生成1000条访问数据
		for(int i = 0; i < 1000; i++) {
			// 生成时间戳
			long timestamp = new Date().getTime();
			
			// 生成随机userid（默认1000注册用户，每天1/10的访客是未注册用户）
			Long userid = 0L;
			
			int newOldUser = newOldUserArr[random.nextInt(10)];
			if(newOldUser == 1) {
				userid = null;
			} else {
				userid = (long) random.nextInt(1000);
			}
			
			// 生成随机pageid（总共1k个页面）
			Long pageid = (long) random.nextInt(1000);  
			
			// 生成随机版块（总共10个版块）
			String section = sections[random.nextInt(10)]; 
			
			// 生成固定的行为，view
			String action = "view"; 
			 
			buffer.append(date).append("")  
					.append(timestamp).append("")
					.append(userid).append("")
					.append(pageid).append("")
					.append(section).append("")
					.append(action).append("\n");
		}
		
		// 生成10条注册数据
		for(int i = 0; i < 10; i++) {
			// 生成时间戳
			long timestamp = new Date().getTime();
			
			// 新用户都是userid为null
			Long userid = null;

			// 生成随机pageid，都是null
			Long pageid = null;  
			
			// 生成随机版块，都是null
			String section = null; 
			
			// 生成固定的行为，view
			String action = "register"; 
			
			buffer.append(date).append("")  
					.append(timestamp).append("")
					.append(userid).append("")
					.append(pageid).append("")
					.append(section).append("")
					.append(action).append("\n");
		}
		
		PrintWriter pw = null;  
		try {
			pw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream("C:\\Users\\htfeng\\Desktop\\access.log")));
			pw.write(buffer.toString());  
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}
	}
	
}
