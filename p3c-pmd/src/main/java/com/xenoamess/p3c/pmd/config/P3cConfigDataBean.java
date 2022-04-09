package com.xenoamess.p3c.pmd.config;

import com.xenoamess.x8l.ContentNode;
import com.xenoamess.x8l.X8lTree;
import com.xenoamess.x8l.databind.X8lDataBean;
import com.xenoamess.x8l.databind.X8lDataBeanFieldMark;
import com.xenoamess.x8l.dealers.LanguageDealer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.xenoamess.x8l.databind.X8lDataBeanDefaultParser.getLastFromList;
import static java.util.logging.Level.WARNING;

/**
 * @author XenoAmess
 * @date 2020/04/24
 */
public class P3cConfigDataBean implements X8lDataBean {

    @SuppressWarnings("unused")
    public static @NotNull
    Set<String> getContentNodeAsStringSet(@NotNull List<Object> list) {
        Object lastObject = getLastFromList(list);
        if (!(lastObject instanceof ContentNode)) {
            return new HashSet<>(0);
        }
        Set<String> res = ((ContentNode) lastObject).asStringSetTrimmed();
        if (res != null) {
            res.remove("");
        }
        return res;
    }

    @SuppressWarnings("unused")
    public static @NotNull
    HashMap<String, Set<String>> getRuleClassPairBlackListMap(@NotNull List<Object> list) {
        ContentNode node = ((ContentNode) getLastFromList(list));
        if (node == null) {
            return new HashMap<>(0);
        }
        List<ContentNode> contentNodeChildren = node.getContentNodesFromChildren();
        if (contentNodeChildren.size() == 0) {
            return new HashMap<>(0);
        }
        HashMap<String, Set<String>> res = new HashMap<>(contentNodeChildren.size());

        for (ContentNode au : contentNodeChildren) {
            res.put(
                    au.getName().trim(),
                    au.asStringSetTrimmed()
            );
        }
        return res;
    }

    private static final Logger LOGGER = Logger.getLogger(P3cConfigDataBean.class.getName());

    private X8lTree p3cConfigX8lTree;

    @X8lDataBeanFieldMark(
            paths = {
                    "com.alibaba.p3c.pmd.config>rule_blacklist"
            },
            parser = P3cConfigDataBean.class,
            functionName = "getContentNodeAsStringSet"
    )
    private Set<String> ruleBlackListSet;

    @X8lDataBeanFieldMark(
            paths = {
                    "com.alibaba.p3c.pmd.config>class_blacklist"
            },
            parser = P3cConfigDataBean.class,
            functionName = "getContentNodeAsStringSet"
    )
    private Set<String> classBlackListSet;

    @X8lDataBeanFieldMark(
            paths = {
                    "com.alibaba.p3c.pmd.config>package_blacklist"
            },
            parser = P3cConfigDataBean.class,
            functionName = "getContentNodeAsStringSet"
    )
    private Set<String> packageBlackListSet;

    @X8lDataBeanFieldMark(
            paths = {
                    "com.alibaba.p3c.pmd.config>rule_class_pair_blacklist"
            },
            parser = P3cConfigDataBean.class,
            functionName = "getRuleClassPairBlackListMap"
    )
    private Map<String, Set<String>> ruleClassPairBlackListMap;

    public void tryPatchP3cConfigDataBean(
            @NotNull File file,
            @NotNull LanguageDealer languageDealer
    ) {
        if (file.exists() && file.isFile()) {
            try {
                X8lTree patchConfigX8lTree = X8lTree.load(
                        file, languageDealer
                );
                this.getP3cConfigX8lTree().append(patchConfigX8lTree);
                this.loadFromX8lTree(this.getP3cConfigX8lTree());
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

    public Map<String, Set<String>> getRuleClassPairBlackListMap() {
        return ruleClassPairBlackListMap;
    }

    public void setRuleClassPairBlackListMap(Map<String, Set<String>> ruleClassPairBlackListMap) {
        this.ruleClassPairBlackListMap = ruleClassPairBlackListMap;
    }

    public Set<String> getPackageBlackListSet() {
        return packageBlackListSet;
    }

    public void setPackageBlackListSet(Set<String> packageBlackListSet) {
        this.packageBlackListSet = packageBlackListSet;
    }

}
