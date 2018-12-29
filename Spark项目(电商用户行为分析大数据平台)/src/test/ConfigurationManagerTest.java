package com.htfeng.sparkproject.test;

import com.htfeng.sparkproject.conf.ConfigurationManager;

/**
 * 配置管理组件测试类
 * @author htfeng
 *
 */
public class ConfigurationManagerTest {
	public static void main(String[] args) {
		String testkey1 = ConfigurationManager.geProperty("testkey1");
		String testkey2 = ConfigurationManager.geProperty("testkey2");
		System.out.println(testkey1);
		System.out.println(testkey2);
	}
}
