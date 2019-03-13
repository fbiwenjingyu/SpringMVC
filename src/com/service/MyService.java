package com.service;

import com.annotation.SimpleService;

@SimpleService("myService")
public class MyService {
	public String getMyInfo(String name,String age) {
		return "{'name':" + name + ",'age':" + age + "}";
		
	}
	
	public static void main(String[] args) {
		System.out.println(new MyService().getMyInfo("kelvin","24"));
	}

}
