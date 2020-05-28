package com.alibaba.p3c.pmd;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IfVoidIsPrimitiveTest {
    @Test
    public void test() {
        assertTrue(Void.TYPE.isPrimitive());
    }
}
