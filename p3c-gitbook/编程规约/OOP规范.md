## (四) OOP规约 

1. 【强制】避免通过一个类的对象引用访问此类的静态变量或静态方法，无谓增加编译器解析成本，直接用**类名**来访问即可。 
2. 【强制】所有的覆写方法，必须加@Override注解。 
<br><span style="color:orange">说明</span>：getObject()与get0bject()的问题。一个是字母的O，一个是数字的0，加@Override可以准确判断是否覆盖成功。另外，如果在抽象类中对方法签名进行修改，其实现类会马上编译报错。 
3. 【强制】相同参数类型，相同业务含义，才可以使用Java的可变参数，避免使用Object。 
<br><span style="color:orange">说明</span>：可变参数必须放置在参数列表的最后。（提倡同学们尽量不用可变参数编程） 
<br><span style="color:green">正例</span>：
```
public User getUsers(String type, Integer... ids) {...} 
```
4. 【强制】外部正在调用或者二方库依赖的接口，不允许修改方法签名，避免对接口调用方产生影响。接口过时必须加`@Deprecated`注解，并清晰地说明采用的新接口或者新服务是什么。 
5. 【强制】不能使用过时的类或方法。 
<br><span style="color:orange">说明</span>：java.net.URLDecoder 中的方法decode(String encodeStr) 这个方法已经过时，应该使用双参数decode(String source, String encode)。接口提供方既然明确是过时接口，那么有义务同时提供新的接口；作为调用方来说，有义务去考证过时方法的新实现是什么。 
6. 【强制】Object的equals方法容易抛空指针异常，应使用常量或确定有值的对象来调用equals。
<br><span style="color:green">正例</span>："test".equals(object);
<br><span style="color:red">反例</span>：object.equals("test"); 
<br><span style="color:orange">说明</span>：推荐使用java.util.Objects#equals（JDK7引入的工具类）
7. 【强制】所有的相同类型的包装类对象之间值的比较，全部使用equals方法比较。 
<br><span style="color:orange">说明</span>：对于Integer var = ?  在-128至127范围内的赋值，Integer对象是在IntegerCache.cache产生，会复用已有对象，这个区间内的Integer值可以直接使用==进行判断，但是这个区间之外的所有数据，都会在堆上产生，并不会复用已有对象，这是一个大坑，推荐使用equals方法进行判断。 
8. 关于基本数据类型与包装数据类型的使用标准如下：
<br>1） 【强制】所有的POJO类属性必须使用包装数据类型。
<br>2） 【强制】RPC方法的返回值和参数必须使用包装数据类型。
<br>3） 【推荐】所有的局部变量使用基本数据类型。
<br><span style="color:orange">说明</span>：POJO类属性没有初值是提醒使用者在需要使用时，必须自己显式地进行赋值，任何NPE问题，或者入库检查，都由使用者来保证。
<br><span style="color:green">正例</span>：数据库的查询结果可能是null，因为自动拆箱，用基本数据类型接收有NPE风险。
<br><span style="color:red">反例</span>：比如显示成交总额涨跌情况，即正负x%，x为基本数据类型，调用的RPC服务，调用不成功时，返回的是默认值，页面显示为0%，这是不合理的，应该显示成中划线。所以包装数据类型的null值，能够表示额外的信息，如：远程调用失败，异常退出。 
9. 【强制】定义DO/DTO/VO等POJO类时，不要设定任何属性**默认值**。
<br><span style="color:red">反例</span>：POJO类的gmtCreate默认值为new Date();但是这个属性在数据提取时并没有置入具体值，在更新其它字段时又附带更新了此字段，导致创建时间被修改成当前时间。 
10. 【强制】序列化类新增属性时，请不要修改serialVersionUID字段，避免反序列失败；如果完全不兼容升级，避免反序列化混乱，那么请修改serialVersionUID值。 
<br><span style="color:orange">说明</span>：注意serialVersionUID不一致会抛出序列化运行时异常。 
11. 【强制】构造方法里面禁止加入任何业务逻辑，如果有初始化逻辑，请放在init方法中。 
12. 【强制】POJO类必须写toString方法。使用IDE中的工具：source> generate toString时，如果继承了另一个POJO类，注意在前面加一下super.toString。 <br><span style="color:orange">说明</span>：在方法执行抛出异常时，可以直接调用POJO的toString()方法打印其属性值，便于排查问题。 
13. 【推荐】使用索引访问用String的split方法得到的数组时，需做最后一个分隔符后有无内容的检查，否则会有抛IndexOutOfBoundsException的风险。 
<br><span style="color:orange">说明</span>：
```
String str = "a,b,c,,";  
String[] ary = str.split(",");  
// 预期大于3，结果是3 System.out.println(ary.length);
```
14. 【推荐】当一个类有多个构造方法，或者多个同名方法，这些方法应该按顺序放置在一起，便于阅读，此条规则优先于第15条规则。 
15. 【推荐】 类内方法定义的顺序依次是：公有方法或保护方法 > 私有方法 > getter/setter方法。
<br><span style="color:orange">说明</span>：公有方法是类的调用者和维护者最关心的方法，首屏展示最好；保护方法虽然只是子类关心，也可能是“模板设计模式”下的核心方法；而私有方法外部一般不需要特别关心，是一个黑盒实现；因为承载的信息价值较低，所有Service和DAO的getter/setter方法放在类体最后。 
16. 【推荐】setter方法中，参数名称与类成员变量名称一致，this.成员名 = 参数名。在getter/setter方法中，不要增加业务逻辑，增加排查问题的难度。
<br><span style="color:red">反例</span>：
```
  public Integer getData() {      
      if (condition) {  
        return this.data + 100;  
      } else { 
        return this.data - 100; 
      }  
  }
```
17. 【推荐】循环体内，字符串的连接方式，使用StringBuilder的append方法进行扩展。
<br><span style="color:orange">说明</span>：反编译出的字节码文件显示每次循环都会new出一个StringBuilder对象，然后进行append操作，最后通过toString方法返回String对象，造成内存资源浪费。  <br><span style="color:red">反例</span>：
```
  String str = "start";
  for (int i = 0; i < 100; i++) {
      str = str + "hello";      
  }
```
18. 【推荐】final可以声明类、成员变量、方法、以及本地变量，下列情况使用final关键字：
<br>1） 不允许被继承的类，如：String类。
<br>2） 不允许修改引用的域对象，如：POJO类的域变量。
<br>3） 不允许被重写的方法，如：POJO类的setter方法。
<br>4） 不允许运行过程中重新赋值的局部变量。
<br>5） 避免上下文重复使用一个变量，使用final描述可以强制重新定义一个变量，方便更好地进行重构。 
19. 【推荐】慎用Object的clone方法来拷贝对象。 
<br><span style="color:orange">说明</span>：对象的clone方法默认是浅拷贝，若想实现深拷贝需要重写clone方法实现属性对象的拷贝。 
20. 【推荐】类成员与方法访问控制从严：
<br>1） 如果不允许外部直接通过new来创建对象，那么构造方法必须是private。
<br>2） 工具类不允许有public或default构造方法。
<br>3） 类非static成员变量并且与子类共享，必须是protected。
<br>4） 类非static成员变量并且仅在本类使用，必须是private。
<br>5） 类static成员变量如果仅在本类使用，必须是private。
<br>6） 若是static成员变量，必须考虑是否为final。
<br>7） 类成员方法只供类内部调用，必须是private。
<br>8） 类成员方法只对继承类公开，那么限制为protected。 
<br><span style="color:orange">说明</span>：任何类、方法、参数、变量，严控访问范围。过于宽泛的访问范围，不利于模块解耦。思考：如果是一个private的方法，想删除就删除，可是一个public的service成员方法或成员变量，删除一下，不得手心冒点汗吗？变量像自己的小孩，尽量在自己的视线内，变量作用域太大，无限制的到处跑，那么你会担心的。 