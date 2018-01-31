package com.alibaba.p3c.pmd.lang.java.rule.other;

import java.text.SimpleDateFormat;

/**
 * @author: huawen.phw
 * @date: 2018/1/10
 * Description:
 */
public class UseRightCaseForDateFormatRuleTest {

    static final String Pattern = "yyyyMMdd";

    public static void test(String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(Pattern);
        format = new SimpleDateFormat("yyyyMMdd");
        format = new SimpleDateFormat("YYYYmmdd");//vio
        format = new SimpleDateFormat("YYYY/MM/dd");//vio
        format = new SimpleDateFormat("YY/MM/DD");//vio
        format = new SimpleDateFormat(formatStr);
    }

    public static void main(String[] args) {
        test(Pattern);//vio

        test("YYYmmDD");//vio
    }

}
