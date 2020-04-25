package com.alibaba.p3c.pmd.config;

import com.xenoamess.x8l.ContentNode;
import com.xenoamess.x8l.X8lTree;
import com.xenoamess.x8l.databind.X8lDataBean;
import com.xenoamess.x8l.databind.X8lDataBeanFieldMark;
import com.xenoamess.x8l.dealers.X8lDealer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.xenoamess.x8l.databind.X8lDataBeanDefaultParser.getLastFromList;
import static java.util.logging.Level.WARNING;

/**
 * @author XenoAmess
 * @date 2020/04/24
 */
public class P3cConfigDataBean implements X8lDataBean {
    public static @Nullable Set<String> getContentNodeAsStringSet(@NotNull List<Object> list) {
        return ((ContentNode) getLastFromList(list)).asStringCollectionFill(new HashSet<>());
    }

    private static final Logger LOGGER = Logger.getLogger(P3cConfigDataBean.class.getName());

    private X8lTree p3cConfigX8lTree;

    @X8lDataBeanFieldMark(
            path = "com.alibaba.p3c.pmd.config>rule_config",
            functionName = "getObject"
    )
    private ContentNode ruleConfigNode;

    @X8lDataBeanFieldMark(
            path = "com.alibaba.p3c.pmd.config>rule_blacklist",
            parser = P3cConfigDataBean.class,
            functionName = "getContentNodeAsStringSet"
    )
    private Set<String> ruleBlackListSet;

    @X8lDataBeanFieldMark(
            path = "com.alibaba.p3c.pmd.config>class_blacklist",
            parser = P3cConfigDataBean.class,
            functionName = "getContentNodeAsStringSet"
    )
    private Set<String> classBlackListSet;


    public void tryPatchP3cConfigDataBean(@NotNull File file) {
        if (file.exists() && file.isFile()) {
            try {
                X8lTree patchConfigX8lTree = X8lTree.load(
                        file, X8lDealer.INSTANCE
                );
                this.getP3cConfigX8lTree().append(patchConfigX8lTree);
            } catch (IOException e) {
                LOGGER.log(WARNING, "reading config file" + file + " fails, IO fails.", e);
            } catch (Exception e) {
                LOGGER.log(WARNING, "reading config file" + file + "fails, grammar wrong.", e);
            }
        }
    }

    //-----getters and setters


    public X8lTree getP3cConfigX8lTree() {
        return p3cConfigX8lTree;
    }

    public void setP3cConfigX8lTree(X8lTree p3cConfigX8lTree) {
        this.p3cConfigX8lTree = p3cConfigX8lTree;
    }

    public ContentNode getRuleConfigNode() {
        return ruleConfigNode;
    }

    public void setRuleConfigNode(ContentNode ruleConfigNode) {
        this.ruleConfigNode = ruleConfigNode;
    }

    public Set<String> getRuleBlackListSet() {
        return ruleBlackListSet;
    }

    public void setRuleBlackListSet(Set<String> ruleBlackListSet) {
        this.ruleBlackListSet = ruleBlackListSet;
    }

    public Set<String> getClassBlackListSet() {
        return classBlackListSet;
    }

    public void setClassBlackListSet(Set<String> classBlackListSet) {
        this.classBlackListSet = classBlackListSet;
    }
}
