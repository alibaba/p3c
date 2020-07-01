package com.alibaba.p3c.pmd.lang.java.rule.concurrent

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils.LOCK_INTERRUPTIBLY_NAME
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils.LOCK_NAME
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils.UN_LOCK_NAME
import net.sourceforge.pmd.lang.java.ast.ASTBlock
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement
import net.sourceforge.pmd.lang.java.ast.ASTFinallyStatement
import net.sourceforge.pmd.lang.java.ast.ASTName
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression
import net.sourceforge.pmd.lang.java.ast.ASTTryStatement
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode
import java.util.concurrent.locks.Lock

/**
 * @author caikang
 * @date 2019/09/29
 */
open class LockShouldWithTryFinallyRule : AbstractAliRule() {

    override fun visit(node: ASTBlock, data: Any): Any? {
        checkBlock(node, data)
        return super.visit(node, data)
    }

    private fun checkBlock(block: ASTBlock, data: Any) {
        val statements = block.findChildrenOfType(ASTBlockStatement::class.java)
        if (statements.isNullOrEmpty()) {
            return
        }
        var lockExpression: ASTStatementExpression? = null
        for (statement in statements) {
            if (lockExpression != null) {
                // check try finally
                val tryStatement = findNodeByXpath(
                    statement, XPATH_TRY_STATEMENT,
                    ASTTryStatement::class.java
                )
                if (!checkTryStatement(tryStatement)) {
                    addLockViolation(data, lockExpression)
                }
                lockExpression = null
                continue
            }
            // find lock expression
            val expression = findNodeByXpath(
                statement, XPATH_LOCK_STATEMENT,
                ASTStatementExpression::class.java
            ) ?: continue

            if (!expression.isLock) {
                continue
            }
            val astName = expression.getFirstDescendantOfType(ASTName::class.java)
            val lockMethod = astName?.image?.let {
                it.endsWith(".$LOCK_NAME") || it.endsWith(".$LOCK_INTERRUPTIBLY_NAME")
            } ?: false
            if (!lockMethod) {
                continue
            }
            lockExpression = expression
        }
        lockExpression?.let {
            addLockViolation(data, it)
        }
    }

    private fun addLockViolation(data: Any, lockExpression: ASTStatementExpression) {
        addViolationWithMessage(
            data, lockExpression,
            "java.concurrent.LockShouldWithTryFinallyRule.violation.msg",
            arrayOf<Any>(getExpressName(lockExpression))
        )
    }

    private fun checkTryStatement(tryStatement: ASTTryStatement?): Boolean {
        if (tryStatement == null) {
            return false
        }
        val finallyStatement = tryStatement.getFirstChildOfType(ASTFinallyStatement::class.java) ?: return false
        val statementExpression = findNodeByXpath(
            finallyStatement,
            XPATH_UNLOCK_STATEMENT, ASTStatementExpression::class.java
        ) ?: return false

        if (!statementExpression.isLock) {
            return false
        }
        val astName = statementExpression.getFirstDescendantOfType(ASTName::class.java) ?: return false
        return astName.image?.endsWith(".$UN_LOCK_NAME") ?: false
    }

    private fun <T> findNodeByXpath(statement: AbstractJavaNode, xpath: String, clazz: Class<T>): T? {
        val nodes = statement.findChildNodesWithXPath(xpath)
        if (nodes == null || nodes.isEmpty()) {
            return null
        }
        val node = nodes[0]
        return if (!clazz.isAssignableFrom(node.javaClass)) {
            null
        } else clazz.cast(node)
    }

    private fun getExpressName(statementExpression: ASTStatementExpression): String {
        val name = statementExpression.getFirstDescendantOfType(ASTName::class.java)
        return name.image
    }

    companion object {
        private const val XPATH_LOCK_STATEMENT = "Statement/StatementExpression"
        private const val XPATH_UNLOCK_STATEMENT = "Block/BlockStatement/Statement/StatementExpression"
        private const val XPATH_TRY_STATEMENT = "Statement/TryStatement"
    }

    private val ASTStatementExpression?.isLock: Boolean
        get() = this?.type?.let {
            Lock::class.java.isAssignableFrom(it)
        } ?: false
}
