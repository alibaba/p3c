package com.alibaba.p3c.pmd.config;

import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListServiceImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TestConfig {
    @Test
    public void testLoadDefaultConfig() {
        NameListServiceImpl nameListService = new NameListServiceImpl(false);

        assertEquals(
                nameListService.getNameList(
                        "ConstantFieldShouldBeUpperCaseRule",
                        "LOG_VARIABLE_TYPE_SET"
                ),
                new ArrayList<>(Arrays.asList("Log", "Logger"))
        );

        assertEquals(
                nameListService.getNameList(
                        "ConstantFieldShouldBeUpperCaseRule",
                        "WHITE_LIST"
                ),
                new ArrayList<>(Collections.singletonList("serialVersionUID"))
        );

        assertEquals(
                nameListService.getNameList(
                        "LowerCamelCaseVariableNamingRule",
                        "WHITE_LIST"
                ),
                new ArrayList<>(Collections.singletonList("DAOImpl"))
        );

        assertEquals(
                nameListService.getNameList(
                        "PojoMustOverrideToStringRule",
                        "POJO_SUFFIX_SET"
                ),
                new ArrayList<>(Arrays.asList("DO", "DTO", "VO", "BO"))
        );

        assertEquals(
                nameListService.getNameList(
                        "UndefineMagicConstantRule",
                        "LITERAL_WHITE_LIST"
                ),
                new ArrayList<>(Arrays.asList("0", "1", "\"\"", "0.0", "1.0", "-1", "0L", "1L"))
        );

        HashMap<String, String> mapa = new HashMap<>();
        mapa.put("int", "Integer");
        mapa.put("boolean", "Boolean");
        mapa.put("float", "Float");
        mapa.put("double", "Double");
        mapa.put("byte", "Byte");
        mapa.put("short", "Short");
        mapa.put("long", "Long");
        mapa.put("char", "Character");
        mapa.put("void", "Void");
        assertEquals(
                nameListService.getNameMap(
                        "MethodReturnWrapperTypeRule",
                        "PRIMITIVE_TYPE_TO_WRAPPER_TYPE"
                ),
                mapa
        );

        assertEquals(
                nameListService.getNameList(
                        "CollectionInitShouldAssignCapacityRule",
                        "COLLECTION_TYPE"
                ),
                new ArrayList<>(Arrays.asList("HashMap", "ConcurrentHashMap"))
        );

        assertEquals(
                nameListService.getNameList(
                        "ClassNamingShouldBeCamelRule",
                        "CLASS_NAMING_WHITE_LIST"
                ),
                new ArrayList<>(Arrays.asList("Hbase", "HBase", "ID", "ConcurrentHashMap"))
        );

    }
}
