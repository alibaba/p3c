package com.alibaba.p3c.pmd.lang.java.rule.other;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;
import sun.security.krb5.internal.ASRep;

/**
 * @author: huawen.phw
 * @date: 2018/1/9
 * Description:
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
                        if (!StringUtils.isEmpty(image) && !PATTERN.matcher(image).matches()) {
                            addViolationWithMessage(data, argNode, "java.other.UseRightCaseForDateFormatRule.rule.msg",
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

    private static final Pattern PATTERN = Pattern.compile(
        "([yyyy]\\p{javaLowerCase})(.*)([MM]\\p{javaUpperCase})(.*)([dd]\\p{javaLowerCase})(.*)",
        Pattern.DOTALL);
}
