## (三) 其它 
1. 【强制】在使用正则表达式时，利用好其预编译功能，可以有效加快正则匹配速度。 
<br><span style="color:orange">说明</span>：不要在方法体内定义：Pattern pattern = Pattern.compile(规则); 
2. 【强制】velocity调用POJO类的属性时，建议直接使用属性名取值即可，模板引擎会自动按规范调用POJO的getXxx()，如果是boolean基本数据类型变量（boolean命名不需要加is前缀），会自动调用isXxx()方法。 <br><span style="color:orange">说明</span>：注意如果是Boolean包装类对象，优先调用getXxx()的方法。 
3. 【强制】后台输送给页面的变量必须加$!{var}——中间的感叹号。 
<br><span style="color:orange">说明</span>：如果var=null或者不存在，那么${var}会直接显示在页面上。 
4. 【强制】注意 Math.random() 这个方法返回是double类型，注意取值的范围 0≤x<1（能够取到零值，注意除零异常），如果想获取整数类型的随机数，不要将x放大10的若干倍然后取整，直接使用Random对象的`nextInt`或者`nextLong`方法。 
5. 【强制】获取当前毫秒数
<pre>System.currentTimeMillis();</pre> 
而不是
<pre>new Date().getTime();</pre> 
<span style="color:orange">说明</span>：如果想获取更加精确的纳秒级时间值，使用`System.nanoTime()`的方式。在JDK8中，针对统计时间等场景，推荐使用`Instant`类。 
6. 【推荐】不要在视图模板中加入任何复杂的逻辑。 <br><span style="color:orange">说明</span>：根据MVC理论，视图的职责是展示，不要抢模型和控制器的活。 
7. 【推荐】任何数据结构的构造或初始化，都应指定大小，避免数据结构无限增长吃光内存。 
8. 【推荐】及时清理不再使用的代码段或配置信息。 
<br><span style="color:orange">说明</span>：对于垃圾代码或过时配置，坚决清理干净，避免程序过度臃肿，代码冗余。 
<br><span style="color:green">正例</span>：对于暂时被注释掉，后续可能恢复使用的代码片断，在注释代码上方，统一规定使用三个斜杠(`///`)来说明注释掉代码的理由。 