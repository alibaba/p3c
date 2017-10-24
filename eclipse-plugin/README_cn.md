> 首先非常感谢大家对插件的支持与意见，Eclipse的功能相对来说比较简单，希望有更多的同学加入进来一起完善。

## 插件安装
环境：JDK1.8，Eclipse4+。有同学遇到过这样的情况，安装插件重启后，发现没有对应的菜单项，从日志上也看不到相关的异常信息，最后把JDK从1.6升级到1.8解决问题。

Help -> Install New Software...

![](https://gw.alicdn.com/tfscom/TB1LOyPifJNTKJjSspoXXc6mpXa.png)

输入Update Site地址：https://p3c.alibaba.com/plugin/eclipse/update 回车，然后勾选Ali-CodeAnalysis，再一直点Next Next...按提示走下去就好。 然后就是提示重启了，安装完毕。

![](https://gw.alicdn.com/tfscom/TB1Ud5kifBNTKJjSszcXXbO2VXa.png)

注意：有同学反映插件扫描会触发很多 "JPA Java Change Event Handler (Waiting)" 的任务，这个是Eclipse的一个[bug](https://bugs.eclipse.org/bugs/show_bug.cgi?id=387455)，因为插件在扫描的时候会对文件进行标记，所以触发了JPA的任务。卸载JPA插件，或者尝试升级到最新版的Eclipse。附：[JPA project Change Event Handler问题解决](https://my.oschina.net/cimu/blog/278724)


## 插件使用

目前插件实现了开发手册中的53条规则，大部分基于PMD实现，其中有4条规则基于Eclipse实现，支持4条规则的QuickFix功能。

	* 所有的覆写方法，必须加@Override注解， 
 	* if/for/while/switch/do等保留字与左右括号之间都必须加空格,
 	* long或者Long初始赋值时，必须使用大写的L，不能是小写的l）
 	* Object的equals方法容易抛空指针异常，应使用常量或确定有值的对象来调用equals。
 	
目前不支持代码实时检测，需要手动触发，希望更多的人加入进来一起把咱们的插件做得越来越好，尽量提升研发的使用体验。

   
### 代码扫描
可以通过右键菜单、Toolbar按钮两种方式手动触发代码检测。同时结果面板中可以对部分实现了QuickFix功能的规则进行快速修复。

#### 触发扫描
在当前编辑的文件中点击右键，可以在弹出的菜单中触发对该文件的检测。

![](https://gw.alicdn.com/tfscom/TB1XGo8iPihSKJjy0FeXXbJtpXa.png)


在左侧的Project目录树种点击右键，可以触发对整个工程或者选择的某个目录、文件进行检测。 

![](https://gw.alicdn.com/tfscom/TB18UsJi2NZWeJjSZFpXXXjBFXa.png)

   
也可以通过Toolbar中的按钮来触发检测，目前Toolbar的按钮触发的检测范围与您IDE当时的焦点有关，如当前编辑的文件或者是Project目录树选中的项，是不是感觉与右键菜单的检测范围类似呢。 

  ![](https://gw.alicdn.com/tfscom/TB1vt1oifBNTKJjSszcXXbO2VXa.png)

   
#### 扫描结果  
简洁的结果面板，按规则等级分类，等级->规则->文件->违规项。同时还提供一个查看规则详情的界面。

清除结果标记更方便，支持上面提到的4条规则QuickFix。

![](https://gw.alicdn.com/tfscom/TB1_uFJi6ihSKJjy0FlXXadEXXa.png)

#### 查看所有规则
![](https://gw.alicdn.com/tfscom/TB1UNTnmYsTMeJjSszhXXcGCFXa.png)
![](https://gw.alicdn.com/tfscom/TB1_rf7sOAKL1JjSZFoXXagCFXa.png)

#### 国际化

![](https://gw.alicdn.com/tfscom/TB1KsyYsiFTMKJjSZFAXXckJpXa.png) 

![](https://gw.alicdn.com/tfscom/TB19bzdm3oQMeJjy1XaXXcSsFXa.png)
