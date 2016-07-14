package com.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import sun.net.www.ParseUtil;

/**
 * @ClassName: FileToEncodedUrl.java
 * @Description: TODO
 * @author: jie.yang
 * @email: jie.yang@mangocity.com
 * @date: 2016年5月6日 下午1:52:03
 */
public class FileToEncodedUrl {

	public static void main(String[] args) throws IOException {
		URL url = ParseUtil.fileToEncodedURL(new File("src/log4j.properties"));
		File file = new File("src/log4j.properties");
		String path = file.getAbsolutePath();
		System.out.println("path: " + path);
		path = ParseUtil.encodePath(path);
		System.out.println("path: " + path);
	}

}
