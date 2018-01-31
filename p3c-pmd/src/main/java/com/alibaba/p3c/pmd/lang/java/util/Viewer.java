package com.alibaba.p3c.pmd.lang.java.util;

import net.sourceforge.pmd.lang.xpath.Initializer;
import net.sourceforge.pmd.util.viewer.gui.MainFrame;

/**
 * copy from pmd-core:net.sourceforge.pmd.util.viewer.Viewer for AST Viewer
 */
public class Viewer {
    public static void main(String[] args) {
        Initializer.initialize();
        new MainFrame();
    }
}