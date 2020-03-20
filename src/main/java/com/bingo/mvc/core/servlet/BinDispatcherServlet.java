package com.bingo.mvc.core.servlet;

import com.bingo.mvc.annoation.BinAutowired;
import com.bingo.mvc.annoation.BinController;
import com.bingo.mvc.annoation.BinRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description: BinDispatcherServlet负责请求转发
 * User: bingo
 * Date: 2019-07-12
 * Time: 9:18
 */
public class BinDispatcherServlet extends HttpServlet {

    //读取配置文件对象
    private static Properties properties;

    //类名集合
    private static List<String> classNames = new ArrayList<String>();

    //IOC容器
    private static Map<String,Object> ioc = new HashMap<String, Object>();

    //存储类上的mapping，key是类上的mapping，value是该类在ioc的key
    private static Map<String,String> mappings = new HashMap<String, String>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        //init-param标签获取配置文件名称
        String location = config.getInitParameter("location");

        //1.加载配置文件
        loadConfig(location);

        //2.扫描包，将获取的类放入一个集合中
        scanClasses(properties.getProperty("scanPackage"));

        //3.实例化类，并放到IOC容器
        doInstance();

        //4.依赖注入，自动配装类
        try {
            doAutowired();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //将请求url映射与方法对应
        initHandleMapping();
    }

    /**
     * 将请求url映射与方法对应
     */
    private void initHandleMapping() {

        for (Map.Entry<String,Object> entry:ioc.entrySet()){

            String key = entry.getKey();

            Object obj = entry.getValue();

            Class<?> clazz = obj.getClass();

            if(clazz.isAnnotationPresent(BinController.class)){
                String mapping = "";
                String classMapping = "";
                //如果类上有@BinRequestMapping注解，获取其value，就是url
                if(clazz.isAnnotationPresent(BinRequestMapping.class)){
                    BinRequestMapping annotation = clazz.getAnnotation(BinRequestMapping.class);
                    classMapping = annotation.value();
                }

                //获取所有的方法，查找@BinRequestMapping注解
                Method[] methods = clazz.getDeclaredMethods();

                for (Method method: methods){
                    if(method.isAnnotationPresent(BinRequestMapping.class)){
                        method.setAccessible(true);

                        BinRequestMapping annotation = method.getAnnotation(BinRequestMapping.class);

                        String methodMapping = annotation.value();

                        mapping = classMapping+methodMapping;

                        //url作为key，<ioc的key>.methodName作为value
                        mappings.put(mapping, key+"."+method.getName());
                    }
                }
            }
        }
    }

    /**
     * 自动装配
     */
    private void doAutowired() throws IllegalAccessException {

        if(ioc.isEmpty()){
            return;
        }

        for (Map.Entry<String,Object> entry: ioc.entrySet()){

            //String key = entry.getKey();
            Object obj = entry.getValue();

            Class<?> clazz = obj.getClass();

            //拿到每个obj的所有成员变量
            Field[] fields = clazz.getDeclaredFields();

            for (Field field: fields){
                field.setAccessible(true);

                if(field.isAnnotationPresent(BinAutowired.class)){

                    //假如属性是MyService，存入ioc的方式是ioc.put("myService",new MyService());所以取的时候key是myService
                    //field.set(obj,ioc.get(toLowerFirstWord(field.getName())) );


                    String name = field.getName();

                    System.out.println("haha");
                }
            }
        }
    }

    /**
     * 实例化类
     */
    private void doInstance() {
        if(classNames.isEmpty()){
            return;
        }

        for (String className: classNames){
            try {
                Class<?> clazz = Class.forName(className);

                if(clazz.isAnnotationPresent(BinController.class)){
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                }else {
                    continue;
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 把字符串的首字母小写
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name){
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

    /**
     * 扫描包中的所有类，装进一个集合中
     * @param packageName
     */
    private void scanClasses(String packageName) {

        //获取该包所在的目录的全路径
        URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));

        // D:/DevelopSoft/ideaWorkspace/WriteSpringMVC/bingo-mvc/target/bingo-mvc-1.0-SNAPSHOT/WEB-INF/classes/com/bingo/mvc/
        File dir = new File(url.getFile());

        File[] files = dir.listFiles();

        if(files!=null && files.length>0){

            for (File file : files){

                if(file.isDirectory()){
                    scanClasses(packageName+"."+file.getName());
                }else {
                    //包名+类名
                    String className = packageName+"."+file.getName().replace(".class","" );
                    classNames.add(className);
                }
            }
        }
    }

    /**
     * 加载配置文件
     * @param location 配置文件名称
     */
    private void loadConfig(String location) {

        properties = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(location);

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestURI = req.getRequestURI();
        //请求路径将contextPath去掉就是我们想要的路径
        String url = requestURI.replace(req.getContextPath(), "");

        //找不到url直接返回404
        if(!mappings.containsKey(url)){
            resp.getWriter().write("<h1>404 NOT FOUND</h1>");
            return;
        }

        String value = mappings.get(url);

        String[] split = value.split("\\.");

        Object o = ioc.get(split[0]);

        try {
            Method method = o.getClass().getMethod(split[1], HttpServletRequest.class,HttpServletResponse.class);
            method.invoke(o,req,resp);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
