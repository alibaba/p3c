/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package com.alibaba.p3c.pmd.fix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Generated;

import com.alibaba.p3c.pmd.lang.java.util.NumberConstants;
import com.alibaba.p3c.pmd.lang.java.util.StringAndCharConstants;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAdditiveExpression;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTAndExpression;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotationTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTArrayDimsAndInits;
import net.sourceforge.pmd.lang.java.ast.ASTBooleanLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTCastExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalAndExpression;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalOrExpression;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTEqualityExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExclusiveOrExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTInclusiveOrExpression;
import net.sourceforge.pmd.lang.java.ast.ASTInstanceOfExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMarkerAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTMultiplicativeExpression;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTNormalAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTNullLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTPackageDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPostfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPreDecrementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPreIncrementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTReferenceType;
import net.sourceforge.pmd.lang.java.ast.ASTRelationalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTShiftExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSingleMemberAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpressionNotPlusMinus;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.typeresolution.ClassTypeResolver;
import net.sourceforge.pmd.lang.java.typeresolution.PMDASMClassLoader;

//
// Helpful reading:
// http://www.janeg.ca/scjp/oper/promotions.html
// http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html
//

/**
 * 1. custom type resolver，fix bug: resolve type of anonymous class failed 2. set anonymous class to parent's type
 *
 * @author unknown
 * @date 2016/11/21
 */
@Generated("from pmd")
public class FixClassTypeResolver extends ClassTypeResolver {

    private static final Logger LOG = Logger.getLogger(FixClassTypeResolver.class.getName());

    private static final Map<String, Class<?>> PRIMITIVE_TYPES;
    private static final Map<String, String> JAVA_LANG;
    private static final String DOT_STRING = ".";
    private static final String EXCLAMATION = "!";

    static {
        // Note: Assumption here that primitives come from same parent
        // ClassLoader regardless of what ClassLoader we are passed
        Map<String, Class<?>> thePrimitiveTypes = new HashMap<>();
        thePrimitiveTypes.put("void", Void.TYPE);
        thePrimitiveTypes.put(boolean.class.getName(), Boolean.TYPE);
        thePrimitiveTypes.put(byte.class.getName(), Byte.TYPE);
        thePrimitiveTypes.put(char.class.getName(), Character.TYPE);
        thePrimitiveTypes.put(short.class.getName(), Short.TYPE);
        thePrimitiveTypes.put(int.class.getName(), Integer.TYPE);
        thePrimitiveTypes.put(long.class.getName(), Long.TYPE);
        thePrimitiveTypes.put(float.class.getName(), Float.TYPE);
        thePrimitiveTypes.put(double.class.getName(), Double.TYPE);
        PRIMITIVE_TYPES = Collections.unmodifiableMap(thePrimitiveTypes);

        Map<String, String> theJavaLang = new HashMap<>();
        theJavaLang.put("Boolean", "java.lang.Boolean");
        theJavaLang.put("Byte", "java.lang.Byte");
        theJavaLang.put("Character", "java.lang.Character");
        theJavaLang.put("CharSequence", "java.lang.CharSequence");
        theJavaLang.put("Class", "java.lang.Class");
        theJavaLang.put("ClassLoader", "java.lang.ClassLoader");
        theJavaLang.put("Cloneable", "java.lang.Cloneable");
        theJavaLang.put("Comparable", "java.lang.Comparable");
        theJavaLang.put("Compiler", "java.lang.Compiler");
        theJavaLang.put("Double", "java.lang.Double");
        theJavaLang.put("Float", "java.lang.Float");
        theJavaLang.put("InheritableThreadLocal", "java.lang.InheritableThreadLocal");
        theJavaLang.put("Integer", "java.lang.Integer");
        theJavaLang.put("Long", "java.lang.Long");
        theJavaLang.put("Math", "java.lang.Math");
        theJavaLang.put("Number", "java.lang.Number");
        theJavaLang.put("Object", "java.lang.Object");
        theJavaLang.put("Package", "java.lang.Package");
        theJavaLang.put("Process", "java.lang.Process");
        theJavaLang.put("Runnable", "java.lang.Runnable");
        theJavaLang.put("Runtime", "java.lang.Runtime");
        theJavaLang.put("RuntimePermission", "java.lang.RuntimePermission");
        theJavaLang.put("SecurityManager", "java.lang.SecurityManager");
        theJavaLang.put("Short", "java.lang.Short");
        theJavaLang.put("StackTraceElement", "java.lang.StackTraceElement");
        theJavaLang.put("StrictMath", "java.lang.StrictMath");
        theJavaLang.put("String", "java.lang.String");
        theJavaLang.put("StringBuffer", "java.lang.StringBuffer");
        theJavaLang.put("System", "java.lang.System");
        theJavaLang.put("Thread", "java.lang.Thread");
        theJavaLang.put("ThreadGroup", "java.lang.ThreadGroup");
        theJavaLang.put("ThreadLocal", "java.lang.ThreadLocal");
        theJavaLang.put("Throwable", "java.lang.Throwable");
        theJavaLang.put("Void", "java.lang.Void");
        JAVA_LANG = Collections.unmodifiableMap(theJavaLang);
    }

    private final PMDASMClassLoader pmdClassLoader;
    private Map<String, String> importedClasses;
    private List<String> importedOnDemand;
    private int anonymousClassCounter = 0;

    public FixClassTypeResolver() {
        this(FixClassTypeResolver.class.getClassLoader());
    }

    public FixClassTypeResolver(ClassLoader classLoader) {
        pmdClassLoader = PMDASMClassLoader.getInstance(classLoader);
    }

    // FUTURE ASTCompilationUnit should not be a TypeNode. Clean this up
    // accordingly.
    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        String className = null;
        try {
            importedOnDemand = new ArrayList<>();
            importedClasses = new HashMap<>(16);
            className = getClassName(node);
            if (className != null) {
                populateClassName(node, className);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Could not find class " + className + ", due to: " + e);
            }
        } catch (LinkageError e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Could not find class " + className + ", due to: " + e);
            }
        } finally {
            populateImports(node);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTImportDeclaration node, Object data) {
        ASTName importedType = (ASTName)node.jjtGetChild(0);
        if (importedType.getType() != null) {
            node.setType(importedType.getType());
        } else {
            populateType(node, importedType.getImage());
        }

        if (node.getType() != null) {
            node.setPackage(node.getType().getPackage());
        }
        return data;
    }

    @Override
    public Object visit(ASTTypeDeclaration node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTClassOrInterfaceType node, Object data) {
        String typeName = node.getImage();
        populateType(node, typeName);
        return data;
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        populateType(node, node.getImage());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTEnumDeclaration node, Object data) {
        populateType(node, node.getImage());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTAnnotationTypeDeclaration node, Object data) {
        populateType(node, node.getImage());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTName node, Object data) {
        /*
         * Only doing this for nodes where getNameDeclaration is null this means
         * it's not a named node, i.e. Static reference or Annotation Doing this
         * for memory - TODO: Investigate if there is a valid memory concern or
         * not
         */
        if (node.getNameDeclaration() == null) {
            // Skip these scenarios as there is no type to populate in these
            // cases:
            // 1) Parent is a PackageDeclaration, which is not a type
            // 2) Parent is a ImportDeclaration, this is handled elsewhere.
            if (!(node.jjtGetParent() instanceof ASTPackageDeclaration
                || node.jjtGetParent() instanceof ASTImportDeclaration)) {
                String name = node.getImage();
                if (name.indexOf(StringAndCharConstants.DOT) != -1) {
                    name = name.substring(0, name.indexOf(StringAndCharConstants.DOT));
                }
                populateType(node, name);
            }
        } else {
            // Carry over the type from the declaration
            if (node.getNameDeclaration().getNode() instanceof TypeNode) {
                node.setType(((TypeNode)node.getNameDeclaration().getNode()).getType());
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTVariableDeclarator node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTVariableDeclaratorId node, Object data) {
        if (node == null || node.getNameDeclaration() == null) {
            return super.visit(node, data);
        }
        String name = node.getNameDeclaration().getTypeImage();
        if (name != null) {
            populateType(node, name);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTType node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTReferenceType node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTPrimitiveType node, Object data) {
        populateType(node, node.getImage());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTExpression node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTConditionalExpression node, Object data) {
        super.visit(node, data);
        // noinspection StatementWithEmptyBody
        if (node.isTernary()) {
            // TODO Rules for Ternary are complex
        } else {
            rollupTypeUnary(node);
        }
        return data;
    }

    @Override
    public Object visit(ASTConditionalOrExpression node, Object data) {
        populateType(node, boolean.class.getName());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTConditionalAndExpression node, Object data) {
        populateType(node, boolean.class.getName());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTInclusiveOrExpression node, Object data) {
        super.visit(node, data);
        rollupTypeBinaryNumericPromotion(node);
        return data;
    }

    @Override
    public Object visit(ASTExclusiveOrExpression node, Object data) {
        super.visit(node, data);
        rollupTypeBinaryNumericPromotion(node);
        return data;
    }

    @Override
    public Object visit(ASTAndExpression node, Object data) {
        super.visit(node, data);
        rollupTypeBinaryNumericPromotion(node);
        return data;
    }

    @Override
    public Object visit(ASTEqualityExpression node, Object data) {
        populateType(node, boolean.class.getName());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTInstanceOfExpression node, Object data) {
        populateType(node, boolean.class.getName());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTRelationalExpression node, Object data) {
        populateType(node, boolean.class.getName());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTShiftExpression node, Object data) {
        super.visit(node, data);
        // Unary promotion on LHS is type of a shift operation
        rollupTypeUnaryNumericPromotion(node);
        return data;
    }

    @Override
    public Object visit(ASTAdditiveExpression node, Object data) {
        super.visit(node, data);
        rollupTypeBinaryNumericPromotion(node);
        return data;
    }

    @Override
    public Object visit(ASTMultiplicativeExpression node, Object data) {
        super.visit(node, data);
        rollupTypeBinaryNumericPromotion(node);
        return data;
    }

    @Override
    public Object visit(ASTUnaryExpression node, Object data) {
        super.visit(node, data);
        rollupTypeUnaryNumericPromotion(node);
        return data;
    }

    @Override
    public Object visit(ASTPreIncrementExpression node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTPreDecrementExpression node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) {
        super.visit(node, data);
        if (EXCLAMATION.equals(node.getImage())) {
            populateType(node, boolean.class.getName());
        } else {
            rollupTypeUnary(node);
        }
        return data;
    }

    @Override
    public Object visit(ASTPostfixExpression node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTCastExpression node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {
        super.visit(node, data);
        if (node.jjtGetNumChildren() == 1) {
            rollupTypeUnary(node);
        } else {
            // TODO OMG, this is complicated. PrimaryExpression, PrimaryPrefix
            // and PrimarySuffix are all related.
        }
        return data;
    }

    @Override
    public Object visit(ASTPrimaryPrefix node, Object data) {
        super.visit(node, data);
        if (node.getImage() == null) {
            rollupTypeUnary(node);
        } else {
            // TODO OMG, this is complicated. PrimaryExpression, PrimaryPrefix
            // and PrimarySuffix are all related.
        }
        return data;
    }

    @Override
    public Object visit(ASTPrimarySuffix node, Object data) {
        super.visit(node, data);
        // TODO OMG, this is complicated. PrimaryExpression, PrimaryPrefix and
        // PrimarySuffix are all related.
        return data;
    }

    @Override
    public Object visit(ASTNullLiteral node, Object data) {
        // No explicit type
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTBooleanLiteral node, Object data) {
        populateType(node, boolean.class.getName());
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTLiteral node, Object data) {
        super.visit(node, data);
        if (node.jjtGetNumChildren() != 0) {
            rollupTypeUnary(node);
        } else {
            if (node.isIntLiteral()) {
                populateType(node, int.class.getName());
            } else if (node.isLongLiteral()) {
                populateType(node, long.class.getName());
            } else if (node.isFloatLiteral()) {
                populateType(node, float.class.getName());
            } else if (node.isDoubleLiteral()) {
                populateType(node, double.class.getName());
            } else if (node.isCharLiteral()) {
                populateType(node, char.class.getName());
            } else if (node.isStringLiteral()) {
                populateType(node, String.class.getName());
            } else {
                throw new IllegalStateException("PMD error, unknown literal type!");
            }
        }
        return data;
    }

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        super.visit(node, data);
        boolean notRollupTypeUnary = node.jjtGetNumChildren() >= NumberConstants.INTEGER_SIZE_OR_LENGTH_2
            && node.jjtGetChild(1) instanceof ASTArrayDimsAndInits
            || node.jjtGetNumChildren() >= NumberConstants.INTEGER_SIZE_OR_LENGTH_3
            && node.jjtGetChild(NumberConstants.INDEX_2) instanceof ASTArrayDimsAndInits;
        if (!notRollupTypeUnary) {
            rollupTypeUnary(node);
        }
        return data;
    }

    @Override
    public Object visit(ASTStatementExpression node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTNormalAnnotation node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTMarkerAnnotation node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    @Override
    public Object visit(ASTSingleMemberAnnotation node, Object data) {
        super.visit(node, data);
        rollupTypeUnary(node);
        return data;
    }

    /**
     * Roll up the type based on type of the first child node.
     *
     * @param typeNode type node
     */
    private void rollupTypeUnary(TypeNode typeNode) {
        Node node = typeNode;
        if (node.jjtGetNumChildren() >= 1) {
            Node child = node.jjtGetChild(0);
            if (child instanceof TypeNode) {
                typeNode.setType(((TypeNode)child).getType());
            }
        }
    }

    /**
     * Roll up the type based on type of the first child node using Unary
     * Numeric Promotion per JLS 5.6.1
     *
     * @param typeNode type node
     */
    private void rollupTypeUnaryNumericPromotion(TypeNode typeNode) {
        Node node = typeNode;
        if (node.jjtGetNumChildren() >= 1) {
            Node child = node.jjtGetChild(0);
            if (child instanceof TypeNode) {
                Class<?> type = ((TypeNode)child).getType();
                if (type != null) {
                    if (byte.class.getName().equals(type.getName()) || short.class.getName().equals(type.getName())
                        || char.class.getName().equals(type.getName())) {
                        populateType(typeNode, int.class.getName());
                    } else {
                        typeNode.setType(((TypeNode)child).getType());
                    }
                }
            }
        }
    }

    /**
     * Roll up the type based on type of the first and second child nodes using
     * Binary Numeric Promotion per JLS 5.6.2
     *
     * @param typeNode type node
     */
    private void rollupTypeBinaryNumericPromotion(TypeNode typeNode) {
        Node node = typeNode;
        if (node.jjtGetNumChildren() >= NumberConstants.INTEGER_SIZE_OR_LENGTH_2) {
            Node child1 = node.jjtGetChild(0);
            Node child2 = node.jjtGetChild(1);
            if (child1 instanceof TypeNode && child2 instanceof TypeNode) {
                Class<?> type1 = ((TypeNode)child1).getType();
                Class<?> type2 = ((TypeNode)child2).getType();
                if (type1 != null && type2 != null) {
                    // Yeah, String is not numeric, but easiest place to handle
                    // it, only affects ASTAdditiveExpression
                    if (String.class.getName().equals(type1.getName()) || String.class.getName().equals(
                        type2.getName())) {
                        populateType(typeNode, String.class.getName());
                    } else if (boolean.class.getName().equals(type1.getName()) || boolean.class.getName().equals(
                        type2.getName())) {
                        populateType(typeNode, boolean.class.getName());
                    } else if (double.class.getName().equals(type1.getName()) || double.class.getName().equals(
                        type2.getName())) {
                        populateType(typeNode, double.class.getName());
                    } else if (float.class.getName().equals(type1.getName()) || float.class.getName().equals(
                        type2.getName())) {
                        populateType(typeNode, float.class.getName());
                    } else if (long.class.getName().equals(type1.getName()) || long.class.getName().equals(
                        type2.getName())) {
                        populateType(typeNode, long.class.getName());
                    } else {
                        populateType(typeNode, int.class.getName());
                    }
                } else if (type1 != null || type2 != null) {
                    // If one side is known to be a String, then the result is a
                    // String
                    // Yeah, String is not numeric, but easiest place to handle
                    // it, only affects ASTAdditiveExpression
                    boolean populateString = type1 != null && String.class.getName().equals(type1.getName())
                        || type2 != null && String.class.getName().equals(type2.getName());
                    if (populateString) {
                        populateType(typeNode, String.class.getName());
                    }
                }
            }
        }
    }

    private void populateType(TypeNode node, String className) {

        String qualifiedName = className;
        Class<?> myType = PRIMITIVE_TYPES.get(className);
        if (myType == null && importedClasses != null) {
            if (importedClasses.containsKey(className)) {
                qualifiedName = importedClasses.get(className);
            } else if (importedClasses.containsValue(className)) {
                qualifiedName = className;
            }
            if (qualifiedName != null) {
                try {
                    /*
                     * TODO - the map right now contains just class names. if we
                     * use a map of classname/class then we don't have to hit
                     * the class loader for every type - much faster
                     */
                    myType = pmdClassLoader.loadClass(qualifiedName);
                } catch (ClassNotFoundException e) {
                    myType = processOnDemand(qualifiedName);
                } catch (NoClassDefFoundError e) {
                    myType = processOnDemand(qualifiedName);
                } catch (LinkageError e) {
                    myType = processOnDemand(qualifiedName);
                }
            }
        }
        if (myType == null && qualifiedName != null && qualifiedName.contains(DOT_STRING)) {
            // try if the last part defines a inner class
            String qualifiedNameInner = qualifiedName.substring(0,
                qualifiedName.lastIndexOf(StringAndCharConstants.DOT)) + "$"
                + qualifiedName.substring(qualifiedName.lastIndexOf(StringAndCharConstants.DOT) + 1);
            try {
                myType = pmdClassLoader.loadClass(qualifiedNameInner);
            } catch (Exception e) {
                // ignored
            }
        }
        if (myType == null && qualifiedName != null && !qualifiedName.contains(DOT_STRING)) {
            // try again with java.lang....
            try {
                myType = pmdClassLoader.loadClass("java.lang." + qualifiedName);
            } catch (Exception e) {
                // ignored
            }
        }
        if (myType != null) {
            node.setType(myType);
        }
    }

    /**
     * Check whether the supplied class name exists.
     */
    @Override
    public boolean classNameExists(String fullyQualifiedClassName) {
        try {
            pmdClassLoader.loadClass(fullyQualifiedClassName);
            // Class found
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public Class<?> loadClass(String fullyQualifiedClassName) {
        try {
            return pmdClassLoader.loadClass(fullyQualifiedClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private Class<?> processOnDemand(String qualifiedName) {
        for (String entry : importedOnDemand) {
            try {
                return pmdClassLoader.loadClass(entry + "." + qualifiedName);
            } catch (Throwable e) {
            }
        }
        return null;
    }

    private String getClassName(ASTCompilationUnit node) {
        ASTClassOrInterfaceDeclaration classDecl = node.getFirstDescendantOfType(ASTClassOrInterfaceDeclaration.class);
        if (classDecl == null) {
            // Happens if this compilation unit only contains an
            // enum
            return null;
        }
        if (node.declarationsAreInDefaultPackage()) {
            return classDecl.getImage();
        }
        ASTPackageDeclaration pkgDecl = node.getPackageDeclaration();
        importedOnDemand.add(pkgDecl.getPackageNameImage());
        return pkgDecl.getPackageNameImage() + DOT_STRING + classDecl.getImage();
    }

    /**
     * If the outer class wasn't found then we'll get in here
     */
    private void populateImports(ASTCompilationUnit node) {
        List<ASTImportDeclaration> theImportDeclarations = node.findChildrenOfType(ASTImportDeclaration.class);

        importedClasses.putAll(JAVA_LANG);

        // go through the imports
        for (ASTImportDeclaration anImportDeclaration : theImportDeclarations) {
            String strPackage = anImportDeclaration.getPackageName();
            if (anImportDeclaration.isImportOnDemand()) {
                importedOnDemand.add(strPackage);
            } else if (!anImportDeclaration.isImportOnDemand()) {
                String strName = anImportDeclaration.getImportedName();
                importedClasses.put(strName, strName);
                importedClasses.put(strName.substring(strPackage.length() + 1), strName);
            }
        }
    }

    private void populateClassName(ASTCompilationUnit node, String className) throws ClassNotFoundException {
        node.setType(pmdClassLoader.loadClass(className));
        importedClasses.putAll(pmdClassLoader.getImportedClasses(className));
    }

}
