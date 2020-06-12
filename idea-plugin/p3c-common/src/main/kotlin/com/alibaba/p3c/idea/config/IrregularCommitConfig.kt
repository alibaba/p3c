package com.alibaba.p3c.idea.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Created with IntelliJ IDEA.
 * User: Di Jun
 * Date: 2018/7/10
 * Time: 11:00
 * Description:
 */
@State(name = "SmartFoxProjectConfig",
        storages = arrayOf(Storage(file = "${StoragePathMacros.APP_CONFIG}/irregular_commit_info.xml")))
class IrregularCommitConfig : PersistentStateComponent<IrregularCommitConfig> {

    var disableCommitOnIrregular = true

    override fun getState(): IrregularCommitConfig? {
        return this
    }

    override fun loadState(state: IrregularCommitConfig?) {
        if (state == null) {
            return
        }
        XmlSerializerUtil.copyBean(state, this)
    }
}