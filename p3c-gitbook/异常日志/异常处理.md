# 二、异常日志 
## (一) 异常处理 
1. 【强制】Java 类库中定义的可以通过预检查方式规避的RuntimeException异常不应该通过catch 的方式来处理，比如：NullPointerException，IndexOutOfBoundsException等等。 
<span style="color:orange">说明</span>：无法通过预检查的异常除外，比如，在解析字符串形式的数字时，不得不通过catch NumberFormatException来实现。 <br><span style="color:green">正例</span>：<pre>if (obj != null) {...} </pre>
<span style="color:red">反例</span>：
<pre>try { obj.method() } catch (NullPointerException e) {…}</pre>
2. 【强制】异常不要用来做流程控制，条件控制。 
<br><span style="color:orange">说明</span>：异常设计的初衷是解决程序运行中的各种意外情况，且异常的处理效率比条件判断方式要低很多。 
3. 【强制】catch时请分清稳定代码和非稳定代码，稳定代码指的是无论如何不会出错的代码。对于非稳定代码的catch尽可能进行区分异常类型，再做对应的异常处理。 
<br><span style="color:orange">说明</span>：对大段代码进行try-catch，使程序无法根据不同的异常做出正确的应激反应，也不利于定位问题，这是一种不负责任的表现。 
<br><span style="color:green">正例</span>：用户注册的场景中，如果用户输入非法字符，或用户名称已存在，或用户输入密码过于简单，在程序上作出分门别类的判断，并提示给用户。 
4. 【强制】捕获异常是为了处理它，不要捕获了却什么都不处理而抛弃之，如果不想处理它，请将该异常抛给它的调用者。最外层的业务使用者，必须处理异常，将其转化为用户可以理解的内容。 
5. 【强制】有try块放到了事务代码中，catch异常后，如果需要回滚事务，一定要注意手动回滚事务。 
6. 【强制】finally块必须对资源对象、流对象进行关闭，有异常也要做try-catch。 
<br><span style="color:orange">说明</span>：如果JDK7及以上，可以使用try-with-resources方式。 
7. 【强制】不要在finally块中使用return。 
<br><span style="color:orange">说明</span>：finally块中的return返回后方法结束执行，不会再执行try块中的return语句。 
8. 【强制】捕获异常与抛异常，必须是完全匹配，或者捕获异常是抛异常的父类。 
<br><span style="color:orange">说明</span>：如果预期对方抛的是绣球，实际接到的是铅球，就会产生意外情况。 
9. 【推荐】方法的返回值可以为null，不强制返回空集合，或者空对象等，必须添加注释充分
<br><span style="color:orange">说明</span>什么情况下会返回null值。 <br><span style="color:orange">说明</span>：本手册明确防止NPE是调用者的责任。即使被调用方法返回空集合或者空对象，对调用者来说，也并非高枕无忧，必须考虑到远程调用失败、序列化失败、运行时异常等场景返回null的情况。 
10. 【推荐】防止NPE，是程序员的基本修养，注意NPE产生的场景：  
1）返回类型为基本数据类型，return包装数据类型的对象时，自动拆箱有可能产生NPE。     
<span style="color:red">反例</span>：public int f() { return Integer对象}， 如果为null，自动解箱抛NPE。  
2） 数据库的查询结果可能为null。  
3） 集合里的元素即使isNotEmpty，取出的数据元素也可能为null。  
4） 远程调用返回对象时，一律要求进行空指针判断，防止NPE。  
5） 对于Session中获取的数据，建议NPE检查，避免空指针。  
6） 级联调用obj.getA().getB().getC()；一连串调用，易产生NPE。 
<br><span style="color:green">正例</span>：使用JDK8的Optional类来防止NPE问题。 
11. 【推荐】定义时区分unchecked / checked 异常，避免直接抛出new RuntimeException()，更不允许抛出Exception或者Throwable，应使用有业务含义的自定义异常。推荐业界已定义过的自定义异常，如：DAOException / ServiceException等。 
12. 【参考】对于公司外的http/api开放接口必须使用“错误码”；而应用内部推荐异常抛出；跨应用间RPC调用优先考虑使用Result方式，封装isSuccess()方法、“错误码”、“错误简短信息”。 
<br><span style="color:orange">说明</span>：关于RPC方法返回方式使用Result方式的理由：
 <br>1）使用抛异常返回方式，调用方如果没有捕获到就会产生运行时错误。
 2）如果不加栈信息，只是new自定义异常，加入自己的理解的error message，对于调用端解决问题的帮助不会太多。如果加了栈信息，在频繁调用出错的情况下，数据序列化和传输的性能损耗也是问题。 
13. 【参考】避免出现重复的代码（Don’t Repeat Yourself），即DRY原则。 
 <br><span style="color:orange">说明</span>：随意复制和粘贴代码，必然会导致代码的重复，在以后需要修改时，需要修改所有的副本，容易遗漏。必要时抽取共性方法，或者抽象公共类，甚至是组件化。 <br><span style="color:green">正例</span>：一个类中有多个public方法，都需要进行数行相同的参数校验操作，这个时候请抽取： 
 <pre>private boolean checkParam(DTO dto) {...}</pre>