# P3C

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

## <font color="green">Preface</font>
> We are pleased to present Alibaba Java Coding Guidelines which consolidates the best programming practices over the years from Alibaba Group's technical teams. A vast number of Java programming teams impose demanding requirements on code quality across projects as we encourage reuse and better understanding of each other's programs. We have seen many programming problems in the past. For example, defective database table structures and index designs may cause software architecture flaws and performance risks. Another example is confusing code structures being difficult to maintain. Furthermore, vulnerable code without authentication is prone to hackers’ attacks. To address these kinds of problems, we developed this document for Java developers at Alibaba.
 
For more information please refer the *Alibaba Java Coding Guidelines*:
- 中文版: *[阿里巴巴Java开发手册](https://github.com/alibaba/p3c/blob/master/Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C%EF%BC%88%E5%B5%A9%E5%B1%B1%E7%89%88%EF%BC%89.pdf)*
- English Version: *[Alibaba Java Coding Guidelines](https://alibaba.github.io/Alibaba-Java-Coding-Guidelines)*
- 《阿里巴巴Java开发手册》书籍版天猫官方店: *[阿里巴巴Java开发手册最新版](https://detail.tmall.com/item.htm?spm=a220m.1000858.1000725.1.4577601asIhzbW&id=628337143778&areaId=330100&user_id=1932014659&cat_id=2&is_b=1&rn=11a322ef70720cdf5e894001e4b48b55)*
- 《码出高效》书籍版天猫官方店: *[码出高效：Java开发手册](https://detail.tmall.com/item.htm?spm=a230r.1.14.40.7dee7d6bwpO82U&id=575107529181&ns=1&abbucket=20)*

## <font color="green">Introduction</font>
The project consists of 3 parts:  
- [PMD implementations](p3c-pmd)  
- [IntelliJ IDEA plugin](idea-plugin)  
- [Eclipse plugin](eclipse-plugin)   

## <font color="green">Rules</font>
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

