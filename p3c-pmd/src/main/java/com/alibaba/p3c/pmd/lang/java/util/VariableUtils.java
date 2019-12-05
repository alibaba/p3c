package com.alibaba.p3c.pmd.lang.java.util;

import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaAccessNode;

/**
 * @author caikang
 * @date 2019/04/22
 */
public class VariableUtils {
    public static String getVariableName(AbstractJavaAccessNode typeNode) {
        ASTVariableDeclaratorId decl = typeNode.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
        if (decl != null) {
            return decl.getImage();
        }
        return null;
    }
}
