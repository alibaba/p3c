package com.alibaba.p3c.pmd.lang.java.rule.other;

import com.alibaba.p3c.pmd.testframework.ExtendRuleTst;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.testframework.TestDescriptor;
import org.junit.Test;

/**
 * @author: huawen.phw
 * @date: 2018/2/1
 * Description:
 */
public class UseRightCaseForDateFormatRuleTst extends ExtendRuleTst {

    @Test
    public void testExam1() {
        String ruleName = "UseRightCaseForDateFormatRule";
        String examFilePath = "java/" + ruleName + "Exam.java";
        String expectedVioLineNumbers = "16,22,24,26";

        Rule rule = findRule(OtherRulesTest.RULESET, ruleName);
        runTest(rule, examFilePath, expectedVioLineNumbers);
    }

}
