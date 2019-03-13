package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.annotation.SimpleAutoWired;
import com.annotation.SimpleController;
import com.annotation.SimpleRequestMapping;
import com.service.MyService;

@SimpleController("myController")
@SimpleRequestMapping("/request")
public class MyConctroller {
	@SimpleAutoWired("myService")
	private MyService service;
	@SimpleRequestMapping("/print")
	public void request(HttpServletRequest request,HttpServletResponse response,String name,String age) {
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.println(service.getMyInfo(name, age));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
