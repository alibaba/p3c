## （三）代码格式
1. 【强制】大括号的使用约定。如果是大括号内为空，则简洁地写成`{}`即可，不需要换行；如果是非空代码块则：
<br>1） 左大括号前不换行。
<br>2） 左大括号后换行。
<br>3） 右大括号前换行。
<br>4） 右大括号后还有else等代码则不换行；表示终止的右大括号后必须换行。 
2. 【强制】 左小括号和字符之间不出现空格；同样，右小括号和字符之间也不出现空格。详见第5条下方正例提示。
<br><span style="color:red">反例</span>：
```
if (空格a == b空格)
```
3. 【强制】if/for/while/switch/do等保留字与括号之间都必须加空格。 
4. 【强制】任何二目、三目运算符的左右两边都需要加一个空格。 
   <br><span style="color:orange">说明</span>：运算符包括赋值运算符=、逻辑运算符&&、加减乘除符号等。
5. 【强制】采用4个空格缩进，禁止使用tab字符。 
   <br><span style="color:orange">说明</span>：
    如果使用tab缩进，必须设置1个tab为4个空格。IDEA设置tab为4个空格时，请勿勾选`Use tab character`；而在eclipse中，必须勾选`insert spaces for tabs`。 
   <br><span style="color:green">正例</span>： （涉及1-5点）

          public static void main(String[] args) {
              // 缩进4个空格
              String say = "hello";
              // 运算符的左右必须有一个空格
              int flag = 0;
              // 关键词if与括号之间必须有一个空格，括号内的f与左括号，0与右括号不需要空格
              if (flag == 0) {
                  System.out.println(say);
              }
              // 左大括号前加空格且不换行；左大括号后换行
              if (flag == 1) {
                  System.out.println("world");
                  // 右大括号前换行，右大括号后有else，不用换行
              } else {
                  System.out.println("ok");
                  // 在右大括号后直接结束，则必须换行
              }
          }

6. 【强制】注释的双斜线与注释内容之间有且仅有一个空格。 
 <br><span style="color:green">正例</span>：
```
// 这是示例注释，请注意在双斜线之后有一个空格  
String ygb = new String(); 
```
7. 【强制】单行字符数限制不超过120个，超出需要换行，换行时遵循如下原则：
<br>1）第二行相对第一行缩进4个空格，从第三行开始，不再继续缩进，参考示例。
<br>2）运算符与下文一起换行。
<br>3）方法调用的点符号与下文一起换行。
<br>4） 方法调用时，多个参数，需要换行时，在逗号后进行。
<br>5） 在括号前不要换行，见反例。
<br><span style="color:green">正例</span>：
```
StringBuffer sb = new StringBuffer();
     // 超过120个字符的情况下，换行缩进4个空格，点号和方法名称一起换行
    sb.append("zi").append("xin")...
                   .append("huang")...
                   .append("huang")...
                   .append("huang");
```
<br><span style="color:red">反例</span>：
```
StringBuffer sb = new StringBuffer();  
// 超过120个字符的情况下，不要在括号前换行  
sb.append("zi").append("xin")...append      
("huang");    
// 参数很多的方法调用可能超过120个字符，不要在逗号前换行  
method(args1, args2, args3, ... 
, argsX); 
```
8. 【强制】方法参数在定义和传入时，多个参数逗号后边必须加空格。 
<br><span style="color:green">正例</span>：下例中实参的"a",后边必须要有一个空格。 
```
method("a", "b", "c"); 
```
9. 【强制】IDE的text file encoding设置为UTF-8; IDE中文件的换行符使用Unix格式，不要使用Windows格式。 
10. 【推荐】没有必要增加若干空格来使某一行的字符与上一行对应位置的字符对齐。 
<br><span style="color:green">正例</span>： 
```
int a = 3;  
long b = 4L;  
float c = 5F;  
StringBuffer sb = new StringBuffer();
```
<span style="color:orange">说明</span>：增加sb这个变量，如果需要对齐，则给a、b、c都要增加几个空格，在变量比较多的情况下，是非常累赘的事情。 
11. 【推荐】不同逻辑、不同语义、不同业务的代码之间插入一个空行分隔开来以提升可读性。 
<br><span style="color:orange">说明</span>：没有必要插入**多个空行**进行隔开。 