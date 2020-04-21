//package com.alibaba.p3c.idea.pmd;
//
//import com.alibaba.p3c.idea.config.P3cConfig;
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.intellij.openapi.components.ServiceManager;
//import net.sourceforge.pmd.SourceCodeProcessor;
//import net.sourceforge.pmd.*;
//import net.sourceforge.pmd.benchmark.TimeTracker;
//import net.sourceforge.pmd.benchmark.TimedOperationCategory;
//import net.sourceforge.pmd.lang.Language;
//import net.sourceforge.pmd.lang.LanguageVersion;
//import net.sourceforge.pmd.lang.LanguageVersionHandler;
//import net.sourceforge.pmd.lang.Parser;
//import net.sourceforge.pmd.lang.ast.Node;
//
//import java.io.InputStream;
//import java.io.Reader;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.concurrent.TimeUnit;
//
//public class AliSourceCodeProcessor extends SourceCodeProcessor {
//    static P3cConfig smartFoxConfig = ServiceManager.getService(P3cConfig.class);
//    static Cache<String, Node> nodeCache = createNodeCache(smartFoxConfig.getAstCacheTime());
//
//    static void reInitNodeCache(long expireTime) {
//        nodeCache = createNodeCache(expireTime);
//    }
//
//    static Cache createNodeCache(long expireTime) {
//        return CacheBuilder.newBuilder().concurrencyLevel(16)
//                .expireAfterWrite(expireTime, TimeUnit.MILLISECONDS)
//                .maximumSize(100).build();
//    }
//
//    static void invalidateCache(String file) {
//        nodeCache.invalidate(file);
//    }
//
//    protected final PMDConfiguration configuration;
//
//    public AliSourceCodeProcessor(PMDConfiguration configuration) {
//        super(configuration);
//        this.configuration = configuration;
//    }
//
//    private Node getRootNode(Reader sourceCode, RuleSets ruleSets, RuleContext ctx) {
//        if (!smartFoxConfig.getAstCacheEnable()) {
//            return parseNode(ctx, ruleSets, sourceCode);
//        }
//        Node node = nodeCache.getIfPresent(ctx.getSourceCodeFilename());
//        if (node != null) {
//            return node;
//        }
//        return parseNode(ctx, ruleSets, sourceCode);
//    }
//
//    private Node parseNode(RuleContext ctx, RuleSets ruleSets, Reader sourceCode) {
//        LanguageVersion languageVersion = ctx.getLanguageVersion();
//        LanguageVersionHandler languageVersionHandler = languageVersion.getLanguageVersionHandler();
//        Parser parser = PMD.parserFor(languageVersion, configuration);
//        Node rootNode = parse(ctx, sourceCode, parser);
//        symbolFacade(rootNode, languageVersionHandler);
//        Language language = languageVersion.getLanguage();
//        usesDFA(languageVersion, rootNode, ruleSets, language)
//        usesTypeResolution(languageVersion, rootNode, ruleSets, language)
//        nodeCache.put(ctx.getSourceCodeFilename(), rootNode);
//        return rootNode;
//    }
//
//    private static Method symbolFacadeMethod;
//
//    protected void symbolFacade(Node rootNode, LanguageVersionHandler languageVersionHandler) {
//        if (symbolFacadeMethod == null) {
//            try {
//                symbolFacadeMethod = SourceCodeProcessor.class.getDeclaredMethod("symbolFacade",
//                        LanguageVersionHandler.class);
//                symbolFacadeMethod.setAccessible(true);
//            } catch (NoSuchMethodException e) {
//                throw new Error(e);
//            }
//        }
//        try {
//            symbolFacadeMethod.invoke(this, languageVersionHandler);
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            throw new Error(e);
//        }
//    }
//
//    private static Method resolveQualifiedNamesMethod;
//
//    protected void resolveQualifiedNames(Node rootNode, LanguageVersionHandler handler) {
//        if (resolveQualifiedNamesMethod == null) {
//            try {
//                resolveQualifiedNamesMethod = SourceCodeProcessor.class.getDeclaredMethod("resolveQualifiedNamesMethod",
//                        LanguageVersionHandler.class);
//                resolveQualifiedNamesMethod.setAccessible(true);
//            } catch (NoSuchMethodException e) {
//                throw new Error(e);
//            }
//        }
//        try {
//            resolveQualifiedNamesMethod.invoke(this, rootNode, handler);
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            throw new Error(e);
//        }
//    }
//
//}
