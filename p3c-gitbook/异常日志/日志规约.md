## (二) 日志规约 
1. 【强制】应用中不可直接使用日志系统（Log4j、Logback）中的API，而应依赖使用日志框架SLF4J中的API，使用门面模式的日志框架，有利于维护和各个类的日志处理方式统一。 
<pre>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
private static final Logger logger = LoggerFactory.getLogger(Abc.class);  
</pre>
2. 【强制】日志文件推荐至少保存15天，因为有些异常具备以“周”为频次发生的特点。 
3. 【强制】应用中的扩展日志（如打点、临时监控、访问日志等）命名方式：appName_logType_logName.log。logType:日志类型，推荐分类有stats/monitor/visit等；logName:日志描述。这种命名的好处：通过文件名就可知道日志文件属于什么应用，什么类型，什么目的，也有利于归类查找。 
<br><span style="color:green">正例</span>：mppserver应用中单独监控时区转换异常，如：                                 
mppserver_monitor_timeZoneConvert.log 
<br><span style="color:orange">说明</span>：推荐对日志进行分类，如将错误日志和业务日志分开存放，便于开发人员查看，也便于通过日志对系统进行及时监控。 
4. 【强制】对trace/debug/info级别的日志输出，必须使用条件输出形式或者使用占位符的方式。 
<br><span style="color:orange">说明</span>：logger.debug("Processing trade with id: " + id + " and symbol: " + symbol); 如果日志级别是warn，上述日志不会打印，但是会执行字符串拼接操作，如果symbol是对象，会执行toString()方法，浪费了系统资源，执行了上述操作，最终日志却没有打印。 
<br><span style="color:green">正例</span>：
    <pre>（条件） 
      if (logger.isDebugEnabled()) {    
      logger.debug("Processing trade with id: " + id + " and symbol: " + symbol);   
      }  </pre>     
<br><span style="color:green">正例</span>：
    <pre>（占位符） 
          logger.debug("Processing trade with id: {} and symbol : {} ", id, symbol);  
    </pre>
5. 【强制】避免重复打印日志，浪费磁盘空间，务必在log4j.xml中设置additivity=false。 
<br><span style="color:green">正例</span>：
      `<logger name="com.taobao.dubbo.config" additivity="false">`
6. 【强制】异常信息应该包括两类信息：案发现场信息和异常堆栈信息。如果不处理，那么通过关键字throws往上抛出。 
<br><span style="color:green">正例</span>：
<pre>logger.error(各类参数或者对象toString + "_" + e.getMessage(), e);</pre> 
7. 【推荐】谨慎地记录日志。生产环境禁止输出debug日志；有选择地输出info日志；如果使用warn来记录刚上线时的业务行为信息，一定要注意日志输出量的问题，避免把服务器磁盘撑爆，并记得及时删除这些观察日志。 <br><span style="color:orange">说明</span>：大量地输出无效日志，不利于系统性能提升，也不利于快速定位错误点。记录日志时请思考：这些日志真的有人看吗？看到这条日志你能做什么？能不能给问题排查带来好处？ 
8. 【推荐】可以使用warn日志级别来记录用户输入参数错误的情况，避免用户投诉时，无所适从。如非必要，请不要在此场景打出error级别，避免频繁报警。
<br><span style="color:orange">说明</span>：注意日志输出的级别，error级别只记录系统逻辑出错、异常或者重要的错误信息。