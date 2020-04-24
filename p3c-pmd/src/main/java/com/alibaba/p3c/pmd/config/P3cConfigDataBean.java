package com.alibaba.p3c.pmd.config;

import com.xenoamess.x8l.X8lTree;
import com.xenoamess.x8l.databind.X8lDataBean;

public class P3cConfigDataBean implements X8lDataBean {
    private X8lTree p3cConfigX8lTree;

    public X8lTree getP3cConfigX8lTree() {
        return p3cConfigX8lTree;
    }

    public void setP3cConfigX8lTree(X8lTree p3cConfigX8lTree) {
        this.p3cConfigX8lTree = p3cConfigX8lTree;
    }
}
