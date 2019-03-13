package com.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.annotation.SimpleAutoWired;
import com.annotation.SimpleController;
import com.annotation.SimpleRequestMapping;
import com.annotation.SimpleService;

/**
 * SpringMVC中心控制器类
 * @author Administrator
 *
 */
@WebServlet(name="dispatcher",urlPatterns= {"/"},loadOnStartup=0)
public class DispatcherServlet extends HttpServlet {
	private static  Map<String,Object> beansMap = new HashMap<String, Object>();
	private static Map<String,Class> clazzMap = new HashMap<String, Class>();
	List<Class> classList = new ArrayList<Class>();
	List<Object> objList = new ArrayList<Object>();
	private Class<?> controllerClass;
	private Object controller;
	private Method controllerMethod;
	private Class serviceClass;
	private Field autoField;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		urlHandle(req);
		autowaireService();
		invokeController(req,resp);
//		PrintWriter writer = resp.getWriter();
//		writer.println("hello world");
//		writer.flush();
//		writer.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		urlHandle(req);
		autowaireService();
		invokeController(req,resp);
//		PrintWriter writer = resp.getWriter();
//		writer.println("hello world");
//		writer.flush();
//		writer.close();
	
	}
	
	/**
	 * Servlet初始化，扫描包下的所有类，将Controller和Service类放入map中,key为类的注解的名字，值为该类的Class对象
	 */
	@Override
	public void init() throws ServletException {
		scanPackage("com");//扫描包，将controller和service
		
	}
	
	/**
	 * 利用反射机制调用Controller的方法
	 * @param req
	 * @param resp
	 */
	private void invokeController(HttpServletRequest req, HttpServletResponse resp) {
		// 1.获取请求参数                          http://localhost:8080/SpringMVC/request/print?name=kelvin&age=23
		String queryString = req.getQueryString();
		String[] strings = queryString.split("&");
		Map<String,String> paramMap = new HashMap<String, String>();
		for(String s : strings) {
			String[] param = s.split("=");
			String paramName = param[0];
			String paramValue = param[1];
			paramMap.put(paramName, paramValue);
		}
		Collection<String> values = paramMap.values();
		List<String> valueList = new ArrayList<String>(values);
		// 2.调用Controller的方法
		try {
			controllerMethod.invoke(controller, new Object[] {req,resp,valueList.get(0),valueList.get(1)});
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 自动装配service，即将service注入到Controller中
	 */
	private void autowaireService() {
		String serviceName = null;
		Field[] fields = controllerClass.getDeclaredFields();
		for(Field filed : fields) {
			if(filed.isAnnotationPresent(SimpleAutoWired.class)) {
				serviceName = filed.getAnnotation(SimpleAutoWired.class).value();
				autoField = filed;
				break;
			}
		}
		
		serviceClass = clazzMap.get(serviceName);
		try {
			autoField.setAccessible(true);
			autoField.set(controller, serviceClass.newInstance());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	/**
	 * 通过http请求路径找出与请求路径对应的Controller和Controller的方法
	 * @param req
	 */
	private void urlHandle(HttpServletRequest req) {
		String requestURI = req.getRequestURI();   //    /SpringMVC/request/print
		String context = req.getServletContext().getContextPath();  // /SpringMVC
		String requestPath = requestURI.replaceAll(context, ""); // /request/print
		String controllerClassRequestMappingName = requestPath.substring(0, requestPath.lastIndexOf("/")); //  /request
		String controllerMethodRequestMappingName = requestPath.replaceAll(controllerClassRequestMappingName, ""); //  /print
		classList = new ArrayList(clazzMap.values());
		objList = new ArrayList(beansMap.values());
		controllerClass = findControllerByMappingName(classList,controllerClassRequestMappingName);
		try {
			controller = controllerClass.newInstance();
			controllerMethod = findControllerMethodByMappingName(controllerClass,controllerMethodRequestMappingName);
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 通过SimpleRequestMapping注解的名字找到方法
	 * @param controllerClass  被SimpleRequestMapping注解的方法所在的Controller类
	 * @param controllerMethodRequestMappingName 注解的名字
	 * @return
	 */
	private Method findControllerMethodByMappingName(Class<?> controllerClass,
			String controllerMethodRequestMappingName) {
		Method[] methods = controllerClass.getDeclaredMethods();
		for(Method method : methods) {
			if(method.isAnnotationPresent(SimpleRequestMapping.class)) {
				SimpleRequestMapping mapping = method.getAnnotation(SimpleRequestMapping.class);
				if(mapping.value().equals(controllerMethodRequestMappingName)) {
					return method;
				}
			}
		}
		return null;
	}
	
	/**
	 * 通过SimpleController注解的名字找到被注解的Controller类
	 * @param classList  类集合
	 * @param controllerClassRequestMappingName Controller类的SimpleRequestMapping注解的名字
	 * @return
	 */
	private Class<?> findControllerByMappingName(List<Class> classList, String controllerClassRequestMappingName) {
		for(Class controllerClass : classList) {
			if(controllerClass.isAnnotationPresent(SimpleController.class)) {
				if(controllerClass.isAnnotationPresent(SimpleRequestMapping.class)) {
					SimpleRequestMapping mapping = (SimpleRequestMapping) controllerClass.getAnnotation(SimpleRequestMapping.class);
					if(mapping.value().equals(controllerClassRequestMappingName)) { //   /request
						SimpleController controllerMapping =  (SimpleController) controllerClass.getAnnotation(SimpleController.class);
						String controllerName = controllerMapping.value();
						return clazzMap.get(controllerName);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 递归查找指定包名及其子包下的类，并把他们存入hashmap中，key为类的注解的名字，值为该类的Class对象
	 * @param basePackage 包名
	 */
	public void scanPackage(String basePackage) {
		basePackage = basePackage.replaceAll("\\.", "/");
		String path2 = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String path = path2 + basePackage;
		String fileStr = path;
		File file = new File(fileStr);
		File[] files = file.listFiles();
		ClassLoader load = Thread.currentThread().getContextClassLoader();
		for(File f : files) {
			if(f.isDirectory()) {
				String subPackagePath = basePackage + "/" + f.getName();
				scanPackage(subPackagePath);
			}else if(f.isFile()) {
				String className = basePackage.replaceAll("/", "\\.") + "." +  f.getName();
				className = className.substring(0,className.lastIndexOf("."));
				
				try {
					Class<?> clazz = load.loadClass(className);
					if(clazz.isAnnotationPresent(SimpleController.class)) {
						String beanName = clazz.getAnnotation(SimpleController.class).value();
						clazzMap.put(beanName, clazz);
						beansMap.put(beanName, clazz.newInstance());
						 
					}else if(clazz.isAnnotationPresent(SimpleService.class)) {
						String beanName = clazz.getAnnotation(SimpleService.class).value();
						clazzMap.put(beanName, clazz);
						beansMap.put(beanName, clazz.newInstance());
					}
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				continue;
			}
		}
	}
	

	
	public static void main(String[] args) {
		new DispatcherServlet().scanPackage("com");
		Set<String> keySet = clazzMap.keySet();
		for(String className : keySet) {
			System.out.println(className);
		}
	}
	
	
}
