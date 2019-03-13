package com.test;

import java.io.File;

public class FileTest {

	public static void main(String[] args) {
		File f = new File("c:\\test\\test.txt");
		System.out.println(f.getName());
		String className = "c:\\test\\" +  f.getName();
		className = className.substring(0,className.lastIndexOf("."));
		System.out.println(className);
		
		

	}

}
