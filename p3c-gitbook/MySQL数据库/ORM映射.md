## (四) ORM映射
1. 【强制】在表查询中，一律不要使用 * 作为查询的字段列表，需要哪些字段必须明确写明。 
<br/><span style="color:orange">说明</span>：
<br/>1）增加查询分析器解析成本。
<br/>2）增减字段容易与 resultMap 配置不一致。
<br/>3）无用字段增加网络消耗，尤其是 text 类型的字段。

2. 【强制】POJO类的布尔属性不能加is，而数据库字段必须加is_，要求在resultMap中进行字段与属性之间的映射。 
<br/><span style="color:orange">说明</span>：参见定义POJO类以及数据库字段定义规定，在mapper.xml中增加映射，是必须的。在MyBatis Generator生成的代码中，需要进行对应的修改。

3. 【强制】不要用resultClass当返回参数，即使所有类属性名与数据库字段一一对应，也需要定义<resultMap>；反过来，每一个表也必然有一个<resultMap>与之对应。 
<br><span style="color:orange">说明</span>：配置映射关系，使字段与DO类解耦，方便维护。 

4. 【强制】sql.xml配置参数使用：#{}，#param# 不要使用${} 此种方式容易出现SQL注入。 

5. 【强制】iBATIS自带的queryForList(String statementName,int start,int size)不推荐使用。
<br/><span style="color:orange">说明</span>：其实现方式是在数据库取到statementName对应的SQL语句的所有记录，再通过subList取start,size的子集合。 
<br/><span style="color:green">正例</span>：
```java
    Map<String, Object> map = new HashMap<String, Object>();    
    map.put("start", start);    
    map.put("size", size);
```

6. 【强制】不允许直接拿HashMap与Hashtable作为查询结果集的输出。 
<br/><span style="color:red">反例</span>：某同学为避免写一个\<resultMap\>，直接使用 HashTable 来接收数据库返回结果，结果出现日常
是把 bigint 转成 Long 值，而线上由于数据库版本不一样，解析成 BigInteger，导致线上问题。

7. 【强制】更新数据表记录时，必须同时更新记录对应的gmt_modified字段值为当前时间。

8. 【推荐】不要写一个大而全的数据更新接口。传入为POJO类，不管是不是自己的目标更新字段，都进行update table set c1=value1,c2=value2,c3=value3; 
这是不对的。执行SQL时，不要更新无改动的字段，一是易出错；二是效率低；三是增加binlog存储。 

9. 【参考】`@Transactional`事务不要滥用。事务会影响数据库的QPS，另外使用事务的地方需要考虑各方面的回滚方案，包括缓存回滚、搜索引擎回滚、消息补偿、统计修正等。 

10. 【参考】`<isEqual>`中的compareValue是与属性值对比的常量，一般是数字，表示相等时带上此条件；`<isNotEmpty>`表示不为空且不为null时执行；`<isNotNull>`表示不为null值时执行。  