## (七) 控制语句 
1. 【强制】在一个switch块内，每个case要么通过break/return等来终止，要么注释说明程序将继续执行到哪一个case为止；在一个switch块内，都必须包含一个default语句并且放在最后，即使空代码。 
2. 【强制】在if/else/for/while/do语句中必须使用大括号。即使只有一行代码，避免采用单行的编码方式：
<pre>if (condition) statements;</pre>
3. 【强制】在高并发场景中，避免使用”等于”判断作为中断或退出的条件。 
<br><span style="color:orange">说明</span>：如果并发控制没有处理好，容易产生等值判断被“击穿”的情况，使用大于或小于的区间判断条件来代替。  
<br><span style="color:red">反例</span>：判断剩余奖品数量等于0时，终止发放奖品，但因为并发处理错误导致奖品数量瞬间变成了负数，这样的话，活动无法终止。 
4. 【推荐】表达异常的分支时，少用if-else方式，这种方式可以改写成： 
```
    if (condition) {              
      ...              
      return obj;    
    }   
    // 接着写else的业务逻辑代码; 
```
<br><span style="color:orange">说明</span>：如果非得使用if()...else if()...else...方式表达逻辑，【强制】避免后续代码维护困难，请勿超过3层。
<br><span style="color:green">正例</span>：超过3层的 if-else 的逻辑判断代码可以使用卫语句、策略模式、状态模式等来实现，其中卫语句示例如下： 
```
public void today() {      
  if (isBusy()) {   
      System.out.println(“change time.”);            
      return; 
  }       
  if (isFree()) {  
      System.out.println(“go to travel.”);             
      return;     
  }  
  System.out.println(“stay at home to learn Alibaba Java Coding Guidelines.”);      
  return; 
  } 
```
5. 【推荐】除常用方法（如getXxx/isXxx）等外，不要在条件判断中执行其它复杂的语句，将复杂逻辑判断的结果赋值给一个有意义的布尔变量名，以提高可读性。 
<br><span style="color:orange">说明</span>：很多if语句内的逻辑相当复杂，阅读者需要分析条件表达式的最终结果，才能明确什么样的条件执行什么样的语句，那么，如果阅读者分析逻辑表达式错误呢？ <br><span style="color:green">正例</span>： 
```
// 伪代码如下 final boolean existed = (file.open(fileName, "w") != null) && (...) || (...); 
if (existed) {    
   ... 
}  
```  
<span style="color:red">反例</span>：
```
if ((file.open(fileName, "w") != null) && (...) || (...)) {     
  ... 
}
```
6. 【推荐】循环体中的语句要考量性能，以下操作尽量移至循环体外处理，如定义对象、变量、获取数据库连接，进行不必要的try-catch操作（这个try-catch是否可以移至循环体外）。 
7. 【推荐】避免采用取反逻辑运算符。 
<br><span style="color:orange">说明</span>：取反逻辑不利于快速理解，并且取反逻辑写法必然存在对应的正向逻辑写法。 
<br><span style="color:green">正例</span>：使用if (x < 628) 来表达 x 小于628。
<br><span style="color:red">反例</span>：使用if (!(x >= 628)) 来表达 x 小于628。
8. 【推荐】接口入参保护，这种场景常见的是用作批量操作的接口。 
9. 【参考】下列情形，需要进行参数校验：  
1） 调用频次低的方法。  
2） 执行时间开销很大的方法。此情形中，参数校验时间几乎可以忽略不计，但如果因为参数错误导致中间执行回退，或者错误，那得不偿失。  
3） 需要极高稳定性和可用性的方法。  
4） 对外提供的开放接口，不管是RPC/API/HTTP接口。  
5） 敏感权限入口。 
10. 【参考】下列情形，不需要进行参数校验：
<br>1） 极有可能被循环调用的方法。但在方法说明里必须注明外部参数检查要求。
<br>2） 底层调用频度比较高的方法。毕竟是像纯净水过滤的最后一道，参数错误不太可能到底层才会暴露问题。一般DAO层与Service层都在同一个应用中，部署在同一台服务器中，所以DAO的参数校验，可以省略。
<br>3） 被声明成private只会被自己代码所调用的方法，如果能够确定调用方法的代码传入参数已经做过检查或者肯定不会有问题，此时可以不校验参数。