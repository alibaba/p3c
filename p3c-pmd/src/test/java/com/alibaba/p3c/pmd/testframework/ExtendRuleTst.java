package com.alibaba.p3c.pmd.testframework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.testframework.RuleTst;
import net.sourceforge.pmd.testframework.TestDescriptor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author huawen.phw
 * @date 2018/2/1
 * Description:
 */
public class ExtendRuleTst extends RuleTst {


    public void runTest(Rule rule, String examFilePath, String expectedVioLineNumbers) {
        TestDescriptor descriptor = extractTestsFromJavaFile(rule, examFilePath
            , expectedVioLineNumbers);
        if (descriptor != null) {
            runTest(descriptor);
        }
    }

    /**
     * @param rule
     * @return
     */
    public TestDescriptor extractTestsFromJavaFile(Rule rule) {
        return extractTestsFromJavaFile(rule, "java/" + getCleanRuleName(rule) + ".java");
    }

    /**
     * @param rule
     * @param javaFilePath
     * @return
     */
    public TestDescriptor extractTestsFromJavaFile(Rule rule, String javaFilePath) {
        return extractTestsFromJavaFile(rule, javaFilePath, "");
    }

    public TestDescriptor extractTestsFromJavaFile(Rule rule, String javaFilePath, String expectedLineNumbers) {
        if (StringUtils.isEmpty(javaFilePath)) {
            return null;
        }
        //append file suffix
        if (!javaFilePath.toLowerCase().endsWith(".java")) {
            javaFilePath = javaFilePath + ".java";
        }
        InputStream inputStream = getClass().getResourceAsStream(javaFilePath);
        if (inputStream == null) {
            throw new RuntimeException("Couldn't find " + javaFilePath);
        }

        String fileContents = null;
        try {
            fileContents = IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        if (fileContents == null) {
            return null;
        }
        List<Integer> expectedLineNumber = getExpectedLineNumbers(expectedLineNumbers);
        TestDescriptor descriptor = new TestDescriptor(fileContents, rule.getDescription(),
            expectedLineNumber.size(), rule);
        descriptor.setExpectedLineNumbers(expectedLineNumber);
        return descriptor;
    }

    public List<Integer> getExpectedLineNumbers(String lineNumbers) {
        List<Integer> expectedLineNumbers = new ArrayList<>();
        for (String n : lineNumbers.split(" *, *")) {
            try {
                expectedLineNumbers.add(Integer.valueOf(n));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return expectedLineNumbers;
    }
}
