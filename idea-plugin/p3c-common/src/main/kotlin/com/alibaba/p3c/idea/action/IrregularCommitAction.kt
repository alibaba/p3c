package com.alibaba.p3c.idea.action

import com.alibaba.p3c.idea.compatible.inspection.InspectionProfileService
import com.alibaba.p3c.idea.compatible.inspection.Inspections
import com.alibaba.p3c.idea.config.IrregularCommitConfig
import com.alibaba.p3c.idea.i18n.P3cBundle
import com.alibaba.p3c.idea.inspection.AliBaseInspection
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager

/**
 * Created with IntelliJ IDEA.
 * User: Di Jun
 * Date: 2018/7/10
 * Time: 10:42
 * Description:
 */
class IrregularCommitAction : AnAction() {

    private val startTextKey = "com.alibaba.p3c.action.switch_"

    private val endTextKey = "_irregular_commit"

    override fun actionPerformed(e: AnActionEvent?) {
        val project = e!!.project ?: return
        val irregularCommitConfig = ServiceManager.getService(project, IrregularCommitConfig::class.java)
        val tools = Inspections.aliInspections(project) {
            it.tool is AliBaseInspection
        }
        InspectionProfileService.toggleInspection(project, tools, irregularCommitConfig.disableCommitOnIrregular)
        irregularCommitConfig.disableCommitOnIrregular = !irregularCommitConfig.disableCommitOnIrregular
    }

    override fun update(e: AnActionEvent?) {
        val project = e!!.project ?: return
        val irregularCommitConfig = ServiceManager.getService(IrregularCommitConfig::class.java)
        e.presentation.text = if (irregularCommitConfig.disableCommitOnIrregular) {
            P3cBundle.getMessage("${startTextKey}disable${endTextKey}")
        } else {
            P3cBundle.getMessage("${startTextKey}enable${endTextKey}")
        }
    }
}