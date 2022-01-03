package com.xenoamess.p3c.pmd.lang.java.rule.xenoamess.additional;

import com.xenoamess.p3c.pmd.lang.java.rule.AbstractAliRule;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import org.jaxen.JaxenException;

import java.util.Collections;
import java.util.List;

/**
 * [Mandatory] SneakyThrow must specify what Exception type it can sneak.
 *
 * @author XenoAmess
 * @date 2021/1/03
 */
public class SneakyThrowsWithoutExceptionTypeRule extends AbstractAliRule {

    @Override
    public Object visit(ASTCompilationUnit rootNode, Object data) {

        List<Node> nodeList = Collections.emptyList();
        try {
            nodeList = rootNode.findChildNodesWithXPath(
                    "//ClassOrInterfaceBodyDeclaration"
                            + "[./Annotation"
                            + "["
                            + "(@AnnotationName = 'SneakyThrows' or @AnnotationName = 'lombok.SneakyThrows')"
                            + " and"
                            + " not((.//MemberValuePair/@Image = 'value') or (./SingleMemberAnnotation))"
                            + "]"
                            + "]"
                            + "//MethodDeclaration"
            );
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        for (Node node : nodeList) {
            addViolationWithMessage(
                    data,
                    node,
                    "java.xenoamess.additional.SneakyThrowsWithoutExceptionTypeRule.rule.msg"
            );
        }
        return super.visit(rootNode, data);
    }

}
