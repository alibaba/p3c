package com.alibaba.p3c.pmd.lang.java.rule.other.java;

import java.text.SimpleDateFormat;


/**
 * @author: huawen.phw
 * @date: 2018/2/1
 * Description:
 */
public class UseRightCaseForDateFormatRuleExam {

    private static final String pattern= "yyyyMMdd";

    public void exam_1() {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMDD"); //vio

        format = new SimpleDateFormat("yyyy/MM/dd");

        format = new SimpleDateFormat("yyyy-MM-dd");

        format = new SimpleDateFormat("yyyymmdd"); // vio

        format = new SimpleDateFormat("yyyy-MM-DD"); //vio

        format = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss"); //p2 error

        format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //right

        exam_2(pattern);//can not checked

        exam_2("YYYmmDD");//can not checked
    }

    public void exam_2(String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);//can not checked

        format = new SimpleDateFormat(formatStr);//can not checked
    }

    public  void exam_3(){
    }


}