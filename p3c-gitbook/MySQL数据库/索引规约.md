## (二) 索引规约
1. 【强制】业务上具有唯一特性的字段，即使是多个字段的组合，也必须建成唯一索引。 
<br><span style="color:orange">说明</span>：不要以为唯一索引影响了insert速度，这个速度损耗可以忽略，但提高查找速度是明显的；另外，即使在应用层做了非常完善的校验控制，只要没有唯一索引，根据墨菲定律，必然有脏数据产生。 
2. 【强制】超过三个表禁止join。需要join的字段，数据类型必须绝对一致；多表关联查询时，保证被关联的字段需要有索引。 
<br><span style="color:orange">说明</span>：即使双表join也要注意表索引、SQL性能。 
3. 【强制】在varchar字段上建立索引时，必须指定索引长度，没必要对全字段建立索引，根据实际文本区分度决定索引长度即可。 
<br><span style="color:orange">说明</span>：索引的长度与区分度是一对矛盾体，一般对字符串类型数据，长度为20的索引，区分度会高达90%以上，可以使用count(distinct left(列名, 索引长度))/count(*)的区分度来确定。 
4. 【强制】页面搜索严禁左模糊或者全模糊，如果需要请走搜索引擎来解决。 
<br><span style="color:orange">说明</span>：索引文件具有B-Tree的最左前缀匹配特性，如果左边的值未确定，那么无法使用此索引。
5. 【推荐】如果有order by的场景，请注意利用索引的有序性。order by 最后的字段是组合索引的一部分，并且放在索引组合顺序的最后，避免出现file_sort的情况，影响查询性能。 
<br><span style="color:green">正例</span>：where a=? and b=? order by c; 索引：a_b_c 
<br><span style="color:red">反例</span>：索引中有范围查找，那么索引有序性无法利用，如：WHERE a>10 ORDER BY b; 索引a_b无法排序。 
6. 【推荐】利用覆盖索引来进行查询操作，避免回表。 
<br><span style="color:orange">说明</span>：如果一本书需要知道第11章是什么标题，会翻开第11章对应的那一页吗？目录浏览一下就好，这个目录就是起到覆盖索引的作用。 
<br><span style="color:green">正例</span>：能够建立索引的种类分为主键索引、唯一索引、普通索引三种，而覆盖索引只是一种查询的一种效果，用explain的结果，extra列会出现：using index。 
7. 【推荐】利用延迟关联或者子查询优化超多分页场景。 <br><span style="color:orange">说明</span>：MySQL并不是跳过offset行，而是取offset+N行，然后返回放弃前offset行，返回N行，那当offset特别大的时候，效率就非常的低下，要么控制返回的总页数，要么对超过特定阈值的页数进行SQL改写。 
<br><span style="color:green">正例</span>：先快速定位需要获取的id段，然后再关联：       
<pre>SELECT a.* FROM 表1 a, (select id from 表1 where 条件 LIMIT 100000,20 ) b where a.id=b.id </pre>
8. 【推荐】 SQL性能优化的目标：至少要达到 range 级别，要求是ref级别，如果可以是consts最好。 
<br><span style="color:orange">说明</span>：  
1）consts 单表中最多只有一个匹配行（主键或者唯一索引），在优化阶段即可读取到数据。  
2）ref 指的是使用普通的索引（normal index）。  
3）range 对索引进行范围检索。 <br><span style="color:red">反例</span>：explain表的结果，type=index，索引物理文件全扫描，速度非常慢，这个index级别比较range还低，与全表扫描是小巫见大巫。 
9. 【推荐】建组合索引的时候，区分度最高的在最左边。 <br><span style="color:green">正例</span>：如果where a=? and b=? ，a列的几乎接近于唯一值，那么只需要单建idx_a索引即可。 
<br><span style="color:orange">说明</span>：存在非等号和等号混合判断条件时，在建索引时，请把等号条件的列前置。如：where a>? and b=? 那么即使a的区分度更高，也必须把b放在索引的最前列。 
10. 【推荐】防止因字段类型不同造成的隐式转换，导致索引失效。 
11. 【参考】创建索引时避免有如下极端误解：  1）宁滥勿缺。认为一个查询就需要建一个索引。  2）宁缺勿滥。认为索引会消耗空间、严重拖慢更新和新增速度。  3）抵制惟一索引。认为业务的惟一性一律需要在应用层通过“先查后插”方式解决。 