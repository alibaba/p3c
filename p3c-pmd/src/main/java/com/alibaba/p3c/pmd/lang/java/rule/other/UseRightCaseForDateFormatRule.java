package com.alibaba.p3c.pmd.lang.java.rule.other;

import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;

/**
 * @author: huawen.phw
 * @date: 2018/1/9
 * Description: 日期格式化时，yyyy表示当天所在的年，而大写的YYYY代表是week in which
 * year（JDK7之后引入的概念），意思是当天所在的周属于的年份，一周从周日开始，周六结束，只要本周跨年，返回的YYYY就是下一年。
 */
public class UseRightCaseForDateFormatRule extends AbstractAliRule {

    /**
     * 自行配置要检查的类
     */
    private static final String[] allocationClazzs = new String[] {"SimpleDateFormat"};
    private static final String XPATH = "./Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/*";

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        ASTClassOrInterfaceType allocationClazz = node.getFirstChildOfType(ASTClassOrInterfaceType.class);
        if (allocationClazz != null && allocationClazz.getImage() != null) {
            for (String clazz : allocationClazzs) {
                if (!clazz.equals(allocationClazz.getImage())) {
                    continue;
                }
                try {
                    List<Node> argumentNodes = node.findChildNodesWithXPath(XPATH);
                    for (Node argNode : argumentNodes) {
                        String image = "";
                        if (argNode instanceof ASTLiteral) {
                            image = ((ASTLiteral)argNode).getImage();
                            image = image.replace("\"", "");
                        } else if (argNode instanceof ASTName) {
                            image = ((ASTName)argNode).getImage();
                            image = image.replace("\"", "");
                        }
                        String tmp = image.toLowerCase();
                        /***最小化限定：首先必须是标准的日期格式化字符串，再者，大小写不正确时报告违反*/
                        if (!StringUtils.isEmpty(image) && LOWER_PATTERN.matcher(tmp).matches() && !RIGHT_CASE_PATTERN
                            .matcher(image).matches()) {
                            addViolationWithMessage(data, argNode,
                                "java.other.UseRightCaseForDateFormatRule.rule.msg",
                                new Object[] {image});
                        }
                    }
                } catch (JaxenException e) {
                    System.err.println(e.getMessage());
                    continue;
                }

            }
        }

        return super.visit(node, data);
    }

    /**
     * lower y ** upper M && lower d
     */
    private static final Pattern RIGHT_CASE_PATTERN = Pattern.compile(
        "([yyyy]\\p{Lower})(.*)([MM]\\p{Upper})(.*)([dd]\\p{Lower})(.*)",
        Pattern.DOTALL);
    /***standard date format*/
    private static final Pattern LOWER_PATTERN = Pattern.compile(
        "\\Qyyyy\\E(.*)\\Qmm\\E(.*)\\Qdd\\E(.*)",
        Pattern.DOTALL);
}
