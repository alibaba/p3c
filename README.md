# P3C

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=p3c-idea&metric=alert_status)](https://sonarcloud.io/dashboard?id=p3c-idea)
p3c-cmd

[![idea plugin](https://sonarcloud.io/api/project_badges/measure?project=p3c-idea&metric=alert_status)](https://sonarcloud.io/dashboard?id=p3c-idea)
idea plugin(p3c-common)

## <font color="green">Notice 声明</font>
This Third-party maintenance(TPM) here is forked from original [alibaba/p3c](https://github.com/alibaba/p3c)

Follows Apache license described in [license](license.txt)

Sources can be found https://github.com/XenoAmess/p3c

Releases can be found at https://plugins.jetbrains.com/plugin/14109-alibaba-java-coding-guidelines-xenoamess-tpm-

This TPM aims to help maintain alibaba/p3c, fix bugs, and add improvements, as the original developer is too busy to handle them.

This TPM is NOT created, or maintained, or controlled by any alibaba employee, in other words it is a TPM, but not an official branch.

TPM maintainer XenoAmess is not interested in changing the grammar/rules in p3c guidelines (by now).

TPM maintainer XenoAmess have no knowledge with eclipse plugin development.

TPM maintainer XenoAmess suggest you only create pr for module idea-plugin and p3c-pmd, unless you really have a strong reason.

## <font color="green">Features 特性</font>

1.  This TPM can always run on latest Jetbrains-idea release/EAP.

    本第三方维护版会在任何etbrains-idea release或者EAP更新时保证可用。

2.  This TPM will always use as latest dependencies as possible.

    本第三方维护版会使用尽可能新的依赖版本。

3.  This TPM implements a mechanism for configuring black-list/white-list for rules/classes

    本第三方维护版实现有一套配置系统，可以对源码类/规则进行黑名单/白名单过滤。

4.  This TPM does NOT fully obey p3c rules. 
    Especially for the rule who disable deprecated functions.
    Maintainer XenoAmess will make sure they are usable,
    but have no enough time to eliminate every deprecated functions.

    本第三方维护版不完全遵循p3c，尤其是关于禁止使用deprecated函数的规则。
    维护者XenoAmess会保证函数可用，但是没有充裕的时间保证消灭每一个deprecated函数。

## <font color="green">Preface 前言</font>
> We are pleased to present Alibaba Java Coding Guidelines which consolidates the best programming practices over the years from Alibaba Group's technical teams. A vast number of Java programming teams impose demanding requirements on code quality across projects as we encourage reuse and better understanding of each other's programs. We have seen many programming problems in the past. For example, defective database table structures and index designs may cause software architecture flaws and performance risks. Another example is confusing code structures being difficult to maintain. Furthermore, vulnerable code without authentication is prone to hackers’ attacks. To address these kinds of problems, we developed this document for Java developers at Alibaba.
 
For more information please refer the *Alibaba Java Coding Guidelines*:
- 中文版: *[阿里巴巴Java开发手册](https://github.com/alibaba/p3c/blob/master/Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C%EF%BC%88%E5%B5%A9%E5%B1%B1%E7%89%88%EF%BC%89.pdf)*
- English Version: *[Alibaba Java Coding Guidelines](https://alibaba.github.io/Alibaba-Java-Coding-Guidelines)*
- 《阿里巴巴Java开发手册》书籍版天猫官方店: *[阿里巴巴Java开发手册最新版](https://detail.tmall.com/item.htm?spm=a1z2e.8325951.feedDetail.4.3315431gklIXe&id=562626792765&ns=1&abbucket=14)*
- 《码出高效》书籍版天猫官方店: *[码出高效：Java开发手册](https://detail.tmall.com/item.htm?spm=a230r.1.14.40.7dee7d6bwpO82U&id=575107529181&ns=1&abbucket=20)*

## <font color="green">Introduction 介绍</font>
The project consists of 3 parts:  
- [PMD implementations](p3c-pmd)  
- [IntelliJ IDEA plugin](idea-plugin)  
- [Eclipse plugin](eclipse-plugin)   

## <font color="green">Rules 规则</font>
<font color="blue">Forty-nine rules are realized based on PMD, please refer the P3C-PMD documentation for more detailed information. Four rules are implemented within IDE plugins (IDEA and Eclipse) as follows:</font>  

- ``[Mandatory]`` Using a deprecated class or method is prohibited.  
   Note: For example, decode(String source, String encode) should be used instead of the deprecated method decode(String encodeStr). Once an interface has been deprecated, the interface provider has the obligation to provide a new one. At the same time, client programmers have the obligation to check out what its new implementation is.
   
- ``[Mandatory]`` An overridden method from an interface or abstract class must be marked with @Override annotation.
   Counter example: For getObject() and get0bject(), the first one has a letter 'O', and the second one has a number '0'. To accurately determine whether the overriding is successful, an @Override annotation is necessary. Meanwhile, once the method signature in the abstract class is changed, the implementation class will report a compile-time error immediately.
   
- ``[Mandatory]`` A static field or method should be directly referred by its class name instead of its corresponding object name.

- ``[Mandatory]`` The usage of hashCode and equals should follow:
    1. Override hashCode if equals is overridden.
    2. These two methods must be overridden for Set since they are used to ensure that no duplicate object will be inserted in Set.
    3. These two methods must be overridden if self-defined object is used as the key of Map.
   Note: String can be used as the key of Map since these two methods have been rewritten.

## <font color="green">Config Mechanism 配置机制</font>

see 

[documents](idea-plugin/README.md)

[说明](idea-plugin/README_cn.md)
