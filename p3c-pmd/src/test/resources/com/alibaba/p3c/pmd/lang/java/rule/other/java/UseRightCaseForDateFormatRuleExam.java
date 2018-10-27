package com.alibaba.p3c.pmd.lang.java.rule.other.java;

import java.text.SimpleDateFormat;


/**
 * @author huawen.phw
 * @date 2018/2/1
 * Description:
 */
public class UseRightCaseForDateFormatRuleExam {

    private static final String PATTERN= "yyyyMMdd";

    public void exam1() {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMDD"); //vio

        format = new SimpleDateFormat("yyyy/MM/dd");

        format = new SimpleDateFormat("yyyy-MM-dd");

        format = new SimpleDateFormat("yyyymmdd");

        format = new SimpleDateFormat("yyyy-MM-DD");

        format = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss"); //p2 error

        format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //right

        format = new SimpleDateFormat("yy-MM-DD");

        format = new SimpleDateFormat("YY-MM-DD");//vio

        format = new SimpleDateFormat("YY-md");//vio

        format = new SimpleDateFormat("Yy-md"); //vio

        format = new SimpleDateFormat("yyy-md"); //not checked

        format = new SimpleDateFormat("Y-md"); // not checked

        format = new SimpleDateFormat("y-md"); // not checked

        format = new SimpleDateFormat("dd/MM-YYYY"); //not checked

        exam_2(PATTERN);//can not checked

        exam_2("YYYmmDD");//can not checked
    }

    public void exam2(String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN);//can not checked

        format = new SimpleDateFormat(formatStr);//can not checked
    }

    public  void exam3(){
    }


}