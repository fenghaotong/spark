package cn.spark.study.streaming.upgrade.news;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * 访问日志Kafka Producer
 * @author Administrator
 *
 */
public class AccessProducer extends Thread {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static Random random = new Random();
	private static String[] sections = new String[] {"country", "international", "sport", "entertainment", "movie", "carton", "tv-show", "technology", "internet", "car"};
	private static int[] arr = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	private static String date;
	
	private Producer<Integer, String> producer;
	private String topic;
	
	public AccessProducer(String topic) {
		this.topic = topic;
		producer = new Producer<Integer, String>(createProducerConfig());  
		date = sdf.format(new Date());  
	}
	
	private ProducerConfig createProducerConfig() {
		Properties props = new Properties();
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("metadata.broker.list", "192.168.75.101:9092,192.168.75.102:9092");
		return new ProducerConfig(props);  
	}
	
	public void run() {
		int counter = 0;

		while(true) {
			for(int i = 0; i < 100; i++) {
				String log = null;
				
				if(arr[random.nextInt(10)] == 1) {
					log = getRegisterLog();
				} else {
					log = getAccessLog();
				}
				
				producer.send(new KeyedMessage<Integer, String>(topic, log));
				
				counter++;
				if(counter == 100) {
					counter = 0;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}  
				}
			}
		}
	}
	
	private static String getAccessLog() {
		StringBuffer buffer = new StringBuffer("");  
		
		// 生成时间戳
		long timestamp = new Date().getTime();
		
		// 生成随机userid（默认1000注册用户，每天1/10的访客是未注册用户）
		Long userid = 0L;
		
		int newOldUser = arr[random.nextInt(10)];
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
		
		return buffer.append(date).append(" ")  
				.append(timestamp).append(" ")
				.append(userid).append(" ")
				.append(pageid).append(" ")
				.append(section).append(" ")
				.append(action).toString();
	}
	
	private static String getRegisterLog() {
		StringBuffer buffer = new StringBuffer("");
		
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
		
		return buffer.append(date).append(" ")  
				.append(timestamp).append(" ")
				.append(userid).append(" ")
				.append(pageid).append(" ")
				.append(section).append(" ")
				.append(action).toString();
	}
	
	public static void main(String[] args) {
		AccessProducer producer = new AccessProducer("news-access");    
		producer.start();
	}
	
}
