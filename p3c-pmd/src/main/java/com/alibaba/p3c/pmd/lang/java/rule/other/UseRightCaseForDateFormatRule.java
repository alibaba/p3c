package com.alibaba.p3c.pmd.lang.java.rule.other;

import java.util.List;

import com.alibaba.p3c.pmd.lang.AbstractXpathRule;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;

/**
 * [Mandatory] When doing date formatting, "y" should be written in lowercase for "year" in a pattern statement.
 *
 * Note: When doing date formatting, "yyyy" represents the day in which year, while "YYYY" represents the week in which
 * year (a concept introduced in JDK7). If a week is across two years, the returning "YYYY"represents the next year.
 * Some more points need to be notices:
 * Uppercase "M" stands for month.
 * Lowercase "m" stands for minute.
 * Uppercase "H" stands for 24-hour clock.
 * Lowercase "h" stands for 12-hour clock.
 *
 * Positive Example: Example pattern for date formatting:
 * new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 *
 * Counter Example: Someone applied "YYYY/MM/dd" pattern for date formatting, and the execution result of 2017/12/31 was
 * 2018/12/31, leading to a serious failure.
 *
 * @author huawen.phw
 * @date 2018/1/9
 */
public class UseRightCaseForDateFormatRule extends AbstractXpathRule {

    private static final String NEW_XPATH
        = "//AllocationExpression/ClassOrInterfaceType[@Image='SimpleDateFormat']/../Arguments/ArgumentList"
        + "/Expression/PrimaryExpression/PrimaryPrefix/*";
    private static final String LOW_CASE_4Y = "yyyy";
    private static final String LOW_CASE_2Y = "yy";
    private static final String START_QUOTE = "\"";

    public UseRightCaseForDateFormatRule() {
        setXPath(NEW_XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        checkNode(node, data);
    }

    /**
     * 暂只检查4个y和2个y开头的日期格式化字符串参数，不考虑其他类型
     *
     * @param argNode
     * @param data
     */
    private void checkNode(Node argNode, Object data) {
        String image = "";
        if (argNode instanceof ASTLiteral) {
            image = argNode.getImage();
        }
        // 限定只验证字符串，其他如参数、变量等均不考虑
        if (StringUtils.isEmpty(image) || !image.startsWith(START_QUOTE)) {
            return;
        }
        image = image.replace("\"", "");
        String lowerCaseTmp = image.toLowerCase();
        if (!image.startsWith(LOW_CASE_4Y) && lowerCaseTmp.startsWith(LOW_CASE_4Y)) {
            addViolationWithMessage(data, argNode,
                "java.other.UseRightCaseForDateFormatRule.rule.msg",
                new Object[] {image});
        } else if (!image.startsWith(LOW_CASE_2Y) && lowerCaseTmp.startsWith(LOW_CASE_2Y)) {
            addViolationWithMessage(data, argNode,
                "java.other.UseRightCaseForDateFormatRule.rule.msg",
                new Object[] {image});
        } else {
            //暂不考虑
        }
    }
}
