## （二）常量定义

1. 【强制】不允许任何魔法值（即未经预先定义的常量）直接出现在代码中。
<br><span style="color:red">反例</span>：
```
String key = "Id#taobao_" + tradeId;       
cache.put(key, value); 
```
2. 【强制】long或者Long初始赋值时，使用大写的L，不能是小写的l，小写容易跟数字1混淆，造成误解。 
<br><span style="color:orange">说明</span>：<pre>Long a = 2l;</pre> 写的是数字的`21`，还是Long型的`2`? 
3. 【推荐】不要使用一个常量类维护所有常量，按常量功能进行归类，分开维护。 
<br><span style="color:orange">说明</span>：大而全的常量类，非得使用查找功能才能定位到修改的常量，不利于理解和维护。 
<br><span style="color:green">正例</span>：缓存相关常量放在类CacheConsts下；系统配置相关常量放在类ConfigConsts下。 
4. 【推荐】常量的复用层次有五层：跨应用共享常量、应用内共享常量、子工程内共享常量、包内共享常量、类内共享常量。  
1） 跨应用共享常量：放置在二方库中，通常是client.jar中的constant目录下。
2） 应用内共享常量：放置在一方库中，通常是子模块中的constant目录下。
<br><span style="color:red">反例</span>：易懂变量也要统一定义成应用内共享常量，两位攻城师在两个类中分别定义了表示“是”的变量：
```
    类A中：public static final String YES = "yes";
    类B中：public static final String YES = "y";
    A.YES.equals(B.YES) 预期是true，但实际返回为false，导致线上问题。
```
3） 子工程内部共享常量：即在当前子工程的constant目录下。  
4） 包内共享常量：即在当前包下单独的constant目录下。  
5） 类内共享常量：直接在类内部private static final定义。 
5. 【推荐】如果变量值仅在一个固定范围内变化用enum类型来定义。 说明：如果存在名称之外的延伸属性使用enum类型，下面正例中的数字就是延伸信息，表示一年中的第几个季节。 
 <br><span style="color:green">正例</span>： 
```
  public enum SeasonEnum {   
          SPRING(1), SUMMER(2), AUTUMN(3), WINTER(4);
          int seq; 
          SeasonEnum(int seq){         
              this.seq = seq;     
          } 
  } 
```