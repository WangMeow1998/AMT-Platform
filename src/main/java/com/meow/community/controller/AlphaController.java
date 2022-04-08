package com.meow.community.controller;


import com.meow.community.service.AlphaService;
import com.meow.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello Spring Boot.";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){ //通过controller调用service
        return alphaService.find();
    }
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println("请求方法：" + request.getMethod());
        System.out.println("请求路径：" + request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        System.out.println("---以下是请求的头部：");
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println("---请求头部结束");
        System.out.println(request.getParameter("code"));

        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write("<h1>论坛</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }
    }

    //GET 请求

    //举例获取学生信息，分页显示，当前页为1，每页限制20个人
    //方式1： /students?current=1&limit=20

    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    //方法参数列表中只要保证请求参数名current和方法参数名current一致，容器即可对应
    //RequestParam注解表示，name-前端参数名，required-前端参数是否一定要写，如果不写则用默认值-defaultValue
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit){
        System.out.println("current: " + current);
        System.out.println("current: " + limit);
        return "some students";
    }

    //方式2：/student/123
    //123表示的是id
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println("id: " + id);
        return "a student";
    }

    //POST 请求
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){
        System.out.println("name: " + name);
        System.out.println("age: " + age);
        return "success！";
    }

    //响应动态HTML资源
    //方式1：
    @RequestMapping(path = "/teacher1", method = RequestMethod.GET)
    public ModelAndView getTeacher1(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age","28");
        mav.setViewName("/demo/view"); //对应/resources/templates/demo/view.html
        return mav;
    }
    //方式2：
    @RequestMapping(path = "/teacher2", method = RequestMethod.GET)
    public String getTeacher2(Model model){
        model.addAttribute("name","李四");
        model.addAttribute("age","30");
        return "/demo/view"; //对应/resources/templates/demo/view.html
    }
    //响应JSON数据（异步请求）
    //响应一个JSON数据
    @RequestMapping(path = "emp", method = RequestMethod.GET)
    @ResponseBody //@ResponseBody可以将Map中的数据转为JSON格式
    public Map<String, Object> getEmp(){
        Map<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",23);
        map.put("salary",8000);
        return map;
    }
    //响应一组JSON数据
    @RequestMapping(path = "emps", method = RequestMethod.GET)
    @ResponseBody //@ResponseBody可以将Map中的数据转为JSON格式
    public List<Map<String, Object>> getEmps(){
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",23);
        map.put("salary",8000);
        list.add(map);

        map = new HashMap<>();
        map.put("name","李四");
        map.put("age",24);
        map.put("salary",9000);
        list.add(map);

        map = new HashMap<>();
        map.put("name","王五");
        map.put("age",25);
        map.put("salary",1000);
        list.add(map);
        return list;
    }
    // Cookie
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    //设置Cookie
    public String setCookie(HttpServletResponse response){
        //创建Cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置Cookie的生效范围
        cookie.setPath("/community/alpha");
        //设置Cookie的生存时间
        cookie.setMaxAge(60 * 10); //单位是s，60 * 10 ===》 生存时间为10分钟
        //发送Cookie
        response.addCookie(cookie);
        return "set cookie";
    }
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    //携带Cookie发送请求
    public String getCookie(@CookieValue("code") String cookieCode){
        System.out.println(cookieCode);
        return "get cookie";
    }
    //Session
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    //设置Session
    public String setSession(HttpSession session){ //Spring会自动注入Session对象
        //在Session中添加数据
        session.setAttribute("id",233);
        session.setAttribute("name","meowmm");
        return "set session";
    }
    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    //携带Cookie（sessionId）发送请求
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id")); //利用Key获取Session中的数据
        System.out.println(session.getAttribute("name")); //利用Key获取Session中的数据
        return "get session";
    }
}
