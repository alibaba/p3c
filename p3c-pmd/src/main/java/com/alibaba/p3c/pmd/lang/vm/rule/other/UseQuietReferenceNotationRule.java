/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.p3c.pmd.lang.vm.rule.other;

import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.AbstractXpathRule;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.vm.ast.ASTDirective;
import net.sourceforge.pmd.lang.vm.ast.AbstractVmNode;
import net.sourceforge.pmd.lang.vm.ast.Token;
import net.sourceforge.pmd.lang.vm.ast.VmParserConstants;

/**
 * [Mandatory] Variables must add exclamatory mark when passing to velocity engine from backend, like $!{var}.
 * Note: If attribute is null or does not exist, ${var} will be shown directly on web pages.
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class UseQuietReferenceNotationRule extends AbstractXpathRule {
    /**
     * scan file path pattern
     */
    private static final Pattern ALLOW_FILE_PATTERN = Pattern.compile(".*(template|velocity).*");

    private static final String UT_FILE_NAME = "n/a";
    private static final String MACRO_NAME = "macro";

    /**
     * Check reference between two text nodes. Exclude references scan in method.
     */
    private static final String XPATH =
        "//Reference[matches(@literal, \"^\\$[^!]+\") and ./preceding-sibling::Text and ./following-sibling::Text]";

    public UseQuietReferenceNotationRule() {
        setXPath(XPATH);
    }

    @Override
    public void evaluate(Node node, RuleContext ctx) {
        // Exclude directories other than template and velocity.
        String sourceCodeFilename = ctx.getSourceCodeFilename();

        // If file path is not n/a（unit test），and does not contain 'template', 'velocity'，then skip it。
        if (!UT_FILE_NAME.equals(sourceCodeFilename) && !ALLOW_FILE_PATTERN.matcher(sourceCodeFilename).matches()) {
            return;
        }

        // Exclude references inside macro.
        if (checkMacro(node)) {
            return;
        }

        super.evaluate(node, ctx);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        String name = getIdentifyName((AbstractVmNode)node);
        String text = I18nResources.getMessage("vm.other.UseQuietReferenceNotationRule.violation.msg", name);
        addViolationWithMessage(data, node, text);
    }

    private String getIdentifyName(AbstractVmNode node) {
        Token token = node.getFirstToken();
        StringBuilder sb = new StringBuilder();
        while (token.kind >= VmParserConstants.IDENTIFIER && token.kind < VmParserConstants.RCURLY) {
            if (token.kind != VmParserConstants.LCURLY) {
                sb.append(token.image);
            }
            token = token.next;
        }
        return sb.toString();
    }

    /**
     * Check if reference is inside macro.
     *
     * @param node node
     * @return true/false
     */
    private boolean checkMacro(Node node) {
        List<ASTDirective> directiveParents = node.getParentsOfType(ASTDirective.class);

        for (ASTDirective directiveParent : directiveParents) {
            if (MACRO_NAME.equals(directiveParent.getDirectiveName())) {
                return true;
            }
        }

        return false;
    }

}
