package com.bingo.mvc.test;

import com.bingo.mvc.annoation.BinAutowired;
import com.bingo.mvc.annoation.BinController;
import com.bingo.mvc.annoation.BinRequestMapping;
import com.bingo.mvc.annoation.BinRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: bingo
 * Date: 2019-07-12
 * Time: 9:24
 */

@BinController
@BinRequestMapping("/test")
public class MyController {

    @BinAutowired private MyService myService;

//    @BinRequestMapping("/testMapping")
//    public void testMapping(@BinRequestParam("key") String key, HttpServletRequest request, HttpServletResponse response){
//
//        PrintWriter pw = null;
//        try {
//            pw = response.getWriter();
//            pw.write("hello "+key);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            pw.close();
//        }
//    }

    @BinRequestMapping("/testMapping")
    public void testMapping(HttpServletRequest request, HttpServletResponse response){

        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            pw.write("<h1>hello bingo</h1>");

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            pw.close();
        }
    }
}
