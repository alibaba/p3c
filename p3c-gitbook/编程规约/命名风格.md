# 一、编程规约
##（一）命名风格

1. 【强制】代码中的命名均不能以<strong>下划线或美元符号</strong>开始，也不能以<strong>下划线或美元符号</strong>结束。
  <br><span style="color:red">反例</span>：`_name / __name / $name / name_ / name$ / name__`
2. 【强制】代码中的命名严禁使用拼音与英文混合的方式，更不允许直接使用中文的方式。 
  <br><span style="color:orange">说明</span>：正确的英文拼写和语法可以让阅读者易于理解，避免歧义。注意，即使纯拼音命名方式也要避免采用。 
  <br><span style="color:green">正例</span>：alibaba / taobao / youku / hangzhou 等国际通用的名称，可视同英文。 
  <br><span style="color:red">反例</span>：DaZhePromotion [打折] / getPingfenByName() [评分] / int 某变量 = 3 
3. 【强制】类名使用`UpperCamelCase`风格，但以下情形例外：DO / BO / DTO / VO / AO / PO等。 
 <br><span style="color:green">正例</span>：MarcoPolo / UserDO / XmlService / TcpUdpDeal / TaPromotion 
 <br><span style="color:red">反例</span>：macroPolo / UserDo / XMLService / TCPUDPDeal / TAPromotion 
4. 【强制】方法名、参数名、成员变量、局部变量都统一使用`lowerCamelCase`风格，必须遵从驼峰形式。 
<br><span style="color:green">正例</span>： localValue / getHttpMessage() / inputUserId 
5. 【强制】常量命名全部大写，单词间用下划线隔开，力求语义表达完整清楚，不要嫌名字长。 
<br><span style="color:green">正例</span>：MAX_STOCK_COUNT 
<br><span style="color:red">反例</span>：MAX_COUNT 
6. 【强制】抽象类命名使用Abstract或Base开头；异常类命名使用Exception结尾；测试类命名以它要测试的类名开始，以Test结尾。 
7. 【强制】类型与中括号紧挨相连来定义数组。 
 <br><span style="color:green">正例</span>：定义整形数组<code>int[] arrayDemo;</code> 
 <br><span style="color:red">反例</span>：在main参数中，使用<code>String args[]</code>来定义。 
8. 【强制】POJO类中布尔类型的变量，都不要加is前缀，否则部分框架解析会引起序列化错误。 
 <br><span style="color:red">反例</span>：定义为基本数据类型<code>Boolean isDeleted；</code>的属性，它的方法也是<code>isDeleted()</code>，RPC框架在反向解析的时候，“误以为”对应的属性名称是deleted，导致属性获取不到，进而抛出异常。
9. 【强制】包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词。包名统一使用单数形式，但是类名如果有复数含义，类名可以使用复数形式。 
 <br><span style="color:green">正例</span>：应用工具类包名为com.alibaba.ai.util、类名为MessageUtils（此规则参考spring的框架结构） 
10. 【强制】杜绝完全不规范的缩写，避免望文不知义。 
 <br><span style="color:red">反例</span>：AbstractClass“缩写”命名成AbsClass；condition“缩写”命名成 condi，此类随意缩写严重降低了代码的可阅读性。 
11. 【推荐】为了达到代码自解释的目标，任何自定义编程元素在命名时，使用尽量完整的单词组合来表达其意。 
<br><span style="color:green">正例</span>：从远程仓库拉取代码的类命名为PullCodeFromRemoteRepository。 
<br><span style="color:red">反例</span>：变量int a; 的随意命名方式。 
12. 【推荐】如果模块、接口、类、方法使用了设计模式，在命名时体现出具体模式。 
<br><span style="color:orange">说明</span>：将设计模式体现在名字中，有利于阅读者快速理解架构设计理念。 
<br><span style="color:green">正例</span>：
```
public class OrderFactory;
public class LoginProxy;
public class ResourceObserver; 
```
13. 【推荐】接口类中的方法和属性不要加任何修饰符号（public 也不要加），保持代码的简洁性，并加上有效的Javadoc注释。尽量不要在接口里定义变量，如果一定要定义变量，肯定是与接口方法相关，并且是整个应用的基础常量。 
<br><span style="color:green">正例</span>：接口方法签名void f(); 接口基础常量String COMPANY = "alibaba"; 
<br><span style="color:red">反例</span>：接口方法定义public abstract void f(); 
<br><span style="color:orange">说明</span>：JDK8中接口允许有默认实现，那么这个default方法，是对所有实现类都有价值的默认实现。 
14. 接口和实现类的命名有两套规则：  
   1）【强制】对于Service和DAO类，基于SOA的理念，暴露出来的服务一定是接口，内部的实现类用Impl的后缀与接口区别。 
   <br><span style="color:green">正例</span>：CacheServiceImpl实现CacheService接口。<br>
   2） 【推荐】 如果是形容能力的接口名称，取对应的形容词为接口名（通常是–able的形式）。
   <br><span style="color:green">正例</span>：AbstractTranslator实现 Translatable。 
15. 【参考】枚举类名建议带上Enum后缀，枚举成员名称需要全大写，单词间用下划线隔开。 
<br><span style="color:orange">说明</span>：枚举其实就是特殊的常量类，且构造方法被默认强制是私有。 
<br><span style="color:green">正例</span>：枚举名字为ProcessStatusEnum的成员名称：SUCCESS / UNKNOWN_REASON。 
16. 【参考】各层命名规约：  
  A) Service/DAO层方法命名规约<br>
   1） 获取单个对象的方法用get作前缀。
   <br>2） 获取多个对象的方法用list作前缀。
   <br>3） 获取统计值的方法用count作前缀。    
   4） 插入的方法用save/insert作前缀。    
   5） 删除的方法用remove/delete作前缀。    
   6） 修改的方法用update作前缀。 
  <br>B) 领域模型命名规约 <br>
   1） 数据对象：xxxDO，xxx即为数据表名。    
   2） 数据传输对象：xxxDTO，xxx为业务领域相关的名称。    
   3） 展示对象：xxxVO，xxx一般为网页名称。    
   4） POJO是DO/DTO/BO/VO的统称，禁止命名成xxxPOJO。 