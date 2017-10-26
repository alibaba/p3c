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
package com.alibaba.p3c.pmd.lang.java.rule.util;

import java.util.List;
import java.util.SortedMap;

import net.sourceforge.pmd.lang.ast.Node;

/**
 *
 * @author keriezhang
 * @date 2016/11/21
 *
 */
public class NodeSortUtils {

    /**
     * add node to SortedMap with sequence to determine comment location
     * 
     * @param map sorted map
     * @param nodes nodes
     */
    public static void addNodesToSortedMap(SortedMap<Integer, Node> map, List<? extends Node> nodes) {
        for (Node node : nodes) {
            map.put(generateIndex(node), node);
        }
    }

    /**
     * set order according to node begin line and begin column
     * @param node node to sort
     * @return generated index
     */
    public static int generateIndex(Node node) {
        return (node.getBeginLine() << 16) + node.getBeginColumn();
    }
}
