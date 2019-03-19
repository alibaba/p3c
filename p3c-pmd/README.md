# P3C-PMD

## <font color="green">Build requirements</font>
- JDK 1.7+
- Maven 3

## <font color="green">Use as dependency</font>

### <font color="green">Maven</font>
```xml
<dependency>
    <groupId>com.alibaba.p3c</groupId>
    <artifactId>p3c-pmd</artifactId>
    <version>1.3.6</version>
</dependency>
```
### <font color="green">Gradle</font>
```groovy
compile 'com.alibaba.p3c:p3c-pmd:1.3.6'
```

## <font color="green">Rules</font>

P3C-PMD implements 54 rules involved in *Alibaba Java Coding Guidelines*, based on PMD ([https://github.com/pmd/pmd](https://github.com/pmd/pmd)).

### <font color="green">Concurrency</font>
* 1 ``[Mandatory]`` Customized ThreadLocal variables must be recycled, especially when using thread pools in which threads are often reused. Otherwise, it may affect subsequent business logic and cause unexpected problems such as memory leak.
* 2 ``[Mandatory]`` A meaningful thread name is helpful to trace the error information, so assign a name when creating threads or thread pools.  
Positive example:

    ```java
    public class TimerTaskThread extends Thread {
        public TimerTaskThread(){
            super.setName("TimerTaskThread"); … }
    ```
* 3 ``[Mandatory]`` Threads should be provided by thread pools. Explicitly creating threads is not allowed. 
Note: Using thread pool can reduce the time of creating and destroying thread and save system resource. If we do not use thread pools, lots of similar threads will be created which lead to "running out of memory" or over-switching problems.
* 4 ``[Mandatory]`` A thread pool should be created by ThreadPoolExecutor rather than Executors. These would make the parameters of the thread pool understandable. It would also reduce the risk of running out of system resources.
Note: Below are the problems created by usage of Executors for thread pool creation:  
    1. FixedThreadPool and SingleThreadPool:
         Maximum request queue size Integer.MAX_VALUE. A large number of requests might cause OOM.
    2. CachedThreadPool and ScheduledThreadPool:  
        The number of threads which are allowed to be created is Integer.MAX_VALUE. Creating too many threads might lead to OOM.
* 5 ``[Mandatory]`` SimpleDataFormat is unsafe, do not define it as a static variable. If you have to, lock or Apache DateUtils class must be used.
Positive example: Pay attention to thread-safety when using DateUtils. It is recommended to use below:

    ```java
    private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {  
        @Override  
        protected DateFormat initialValue() {  
            return new SimpleDateFormat("yyyy-MM-dd");  
        }  
    };  
    ```
    Note: In JDK8, Instant can be used to replace Date; likewise Calendar is replaced by LocalDateTime, and SimpleDateFormatter is replaced by DateTimeFormatter.
* 6 ``[Mandatory]`` Run multiple TimeTask by using ScheduledExecutorService rather than Timer, because Timer will kill all running threads in case of failure to catch exceptions.
* 7 ``[Recommended]`` When using CountDownLatch to convert asynchronous operations to synchronous ones, each thread must call countdown method before quitting. Make sure to catch any exception during thread running, to let countdown method be executed. If main thread cannot reach await method, program will return until timeout.
Note: Be careful, exception thrown by a child thread cannot be caught by main thread.
* 8 ``[Recommended]`` Avoid using Random instance by multiple threads. Although it is thread-safe, competition on the same seed will damage performance.
Note: Random instance includes instances of java.util.Random and Math.random().  
Positive example:  
After JDK7, ThreadLocalRandom API can be used directly. But before JDK7, instance needs to be created in each thread.

### <font color="green">Collection</font>

* 1 ``[Mandatory]`` Do not cast subList in class ArrayList, otherwise ClassCastException will be thrown: java.util.RandomAccessSubList cannot be cast to java.util.ArrayList.  
Note: subList of ArrayList is an inner class, which is a view of ArrayList. All operations on the Sublist will affect the original list finally.
* 2 ``[Mandatory]`` When using subList, be careful while modifying the size of original list. It might cause ConcurrentModificationException when performing traversing, adding or deleting on the subList.
* 3 ``[Mandatory]`` Use toArray(T[] array) to convert list to array. The input array type should be the same with the list whose size is list.size().
Counter example: Do not use toArray method without arguments. Since the return type is Object[], ClassCastException will be thrown when casting it to a different array type.
Positive example:

    ```java
        List<String> list = new ArrayList<String>(2);
        list.add("guan");
        list.add("bao");        
        String[] array = new String[list.size()];
        array = list.toArray(array);
    ```
Note: When using toArray method with arguments, if input array size is not large enough, the method will re-assign the size internally, and then return the address of new array. If the size is larger than needed, the value of index[list.size()] will be set to null while other values remain the same. Defining an input with the same size of the list is recommended.
* 4 ``[Mandatory]`` Do not use methods which will modify the list after using Arrays.asList to convert array to list, otherwise methods like add/remove/clear will throw UnsupportedOperationException.
Note: The result of asList is the inner class of Arrays, which does not implement methods to modify itself. Arrays.asList is only a transferred interface, data inside which is stored as an array.

    ```java
    String[] str = new String[] { "a", "b" };
    List<String> list = Arrays.asList(str); 
    ```
Case 1: list.add("c"); will throw a runtime exception.  
Case 2: str[0]= "gujin"; list.get(0) will be modified.
* 5 ``[Mandatory]`` Do not remove or add elements to a collection in a foreach loop. Please use Iterator to remove an item. Iterator object should be synchronized when executing concurrent operations.  
Counter example:

    ```java
    List<String> a = new ArrayList<String>();
    a.add("1");
    a.add("2");
    for (String temp : a) {
        if ("1".equals(temp)) {
            a.remove(temp);
        }
    }
    ```
    Note: If you try to replace "1" with "2", you will get an unexpected result.
Positive example:

    ```java
    Iterator<String> it = a.iterator();
    while (it.hasNext()) {    
            String temp =  it.next();             
            if (delete condition) {              
                  it.remove();       
            }
        }    
    ```
* 6``[Recommended]`` Set a size when initializing a collection if possible.  
Note: Better to use ArrayList(int initialCapacity) to initialize ArrayList.


### <font color="green">Naming Conventions</font>
* 1 ``[Mandatory]`` No identifier name should start or end with an underline or a dollar sign.  
Counter example: _name / __name / $Object / name_ / name$ / Object$

* 2 ``[Mandatory]`` Using Chinese, Pinyin, or Pinyin-English mixed spelling in naming is strictly prohibited. Accurate English spelling and grammar will make the code readable, understandable, and maintainable.
Positive example: alibaba / taobao / youku / Hangzhou. In these cases, Chinese proper names in Pinyin are acceptable.

* 3 ``[Mandatory]`` Class names should be nouns in UpperCamelCase except domain models: DO, BO, DTO, VO, etc.
Positive example: MarcoPolo / UserDO / HtmlDTO / XmlService / TcpUdpDeal / TaPromotion
Counter example: macroPolo / UserDo / HTMLDto / XMLService / TCPUDPDeal / TAPromotion

* 4 ``[Mandatory]`` Method names, parameter names, member variable names, and local variable names should be written in lowerCamelCase.
Positive example: localValue / getHttpMessage() / inputUserId

* 5 ``[Mandatory]`` Constant variable names should be written in upper characters separated by underscores. These names should be semantically complete and clear.
Positive example: MAX_STOCK_COUNT
Counter example: MAX_COUNT

* 6 ``[Mandatory]`` Abstract class names must start with Abstract or Base. Exception class names must end with Exception. Test cases shall start with the class names to be tested and end with Test.

* 7 ``[Mandatory]`` Brackets are a part of an Array type. The definition could be: String[] args;
Counter example: String args[];

* 8 ``[Mandatory]`` Do not add 'is' as prefix while defining Boolean variable, since it may cause a serialization exception in some Java Frameworks.
Counter example: boolean isSuccess; The method name will be isSuccess() and then RPC framework will deduce the variable name as 'success', resulting in a serialization error since it cannot find the correct attribute.

* 9 ``[Mandatory]`` Package should be named in lowercase characters. There should be only one English word after each dot. Package names are always in singular format while class name can be in plural format if necessary.  
Positive example: com.alibaba.open.util can be used as package name for utils;
* 10 There are mainly two rules for interface and corresponding implementation class naming:
    1. ``[Mandatory]`` All Service and DAO classes must be interface based on SOA principle. Implementation class names should be ended with Impl.  
Positive example: CacheServiceImpl to implement CacheService.
    2. ``[Recommended]`` If the interface name is to indicate the ability of the interface, then its name should be adjective.  
Positive example: AbstractTranslator to implement Translatable.

### <font color="green">Constant Conventions</font>

* 1 ``[Mandatory]`` Magic values, except for predefined, are forbidden in coding.
Counter example: String key="Id#taobao_" + tradeId;
* 2 ``[Mandatory]`` 'L' instead of 'l' should be used for long or Long variable because 'l' is easily to be regarded as number 1 in mistake.  
Counter example: Long a=2l, it is hard to tell whether it is number 21 or Long 2.

### <font color="green">OOP</font>
* 1 ``[Mandatory]`` Using a deprecated class or method is prohibited.  
Note: For example, decode(String source, String encode) should be used instead of the deprecated method decode(String encodeStr). Once an interface has been deprecated, the interface provider has the obligation to provide a new one. At the same time, client programmers have the obligation to check out what its new implementation is.
* 2 ``[Mandatory]`` Since NullPointerException can possibly be thrown while calling the equals method of Object, equals should be invoked by a constant or an object that is definitely not null.  
Positive example: "test".equals(object);  
Counter example: object.equals("test");  
Note: java.util.Objects#equals (a utility class in JDK7) is recommended.

* 3 ``[Mandatory]`` The wrapper classes should be compared by equals method rather than by symbol of '==' directly.  
Note: Consider this assignment: Integer var = ?. When it fits the range from -128 to 127, we can use == directly for a comparison. Because the Integer object will be generated by IntegerCache.cache, which reuses an existing object. Nevertheless, when it fits the complementary set of the former range, the Integer object will be allocated in Heap, which does not reuse an existing object. This is an [implementation-level detail](https://docs.oracle.com/javase/specs/jls/se9/html/jls-5.html#jls-5.1.7-300) that should NOT be relied upon. Hence using the equals method is always recommended.
* 4 ``[Mandatory]`` Rules for using primitive data types and wrapper classes:
    1. Members of a POJO class must be wrapper classes.
    2. The return value and arguments of a RPC method must be wrapper classes.
    3. ``[Recommended]`` Local variables should be primitive data types.    
Note: In order to remind the consumer of explicit assignments, there are no initial values for members in a POJO class. As a consumer, you should check problems such as NullPointerException and warehouse entries for yourself.
 Positive example: As the result of a database query may be null, assigning it to a primitive date type will cause a risk of NullPointerException because of Unboxing.  
 Counter example: Consider the output of a transaction volume's amplitude, like ±x%. As a primitive data, when it comes to a failure of calling a RPC service, the default return value: 0% will be assigned, which is not correct. A hyphen like - should be assigned instead. Therefore, the null value of a wrapper class can represent additional information, such as a failure of calling a RPC service, an abnormal exit, etc.
* 5 ``[Mandatory]`` While defining POJO classes like DO, DTO, VO, etc., do not assign any default values to the members.  
* 6 ``[Mandatory]`` The toString method must be implemented in a POJO class. The super.toString method should be called in front of the whole implementation if the current class extends another POJO class.  
Note: We can call the toString method in a POJO directly to print property values in order to check the problem when a method throws an exception in runtime.
* 7 ``[Recommended]`` Use the append method in StringBuilder inside a loop body when concatenating multiple strings.  

    Counter example:

    ```java
    String str = "start";  
    for(int i=0; i<100; i++) {  
        str = str + "hello";  
    }
    ```
    
    Note: According to the decompiled bytecode file, for each iteration, it allocates a new StringBuilder object, appends a string, and finally returns a String object via the toString method. This is a tremendous waste of memory, especially when the iteration count is large.

### <font color="green">Flow Control Statements</font>
* 1 ``[Mandatory]`` In a switch block, each case should be finished by break/return. If not, a note should be included to describe at which case it will stop. Within every switch block, a default statement must be present, even if it is empty.
* 2 ``[Mandatory]`` Braces are used with if, else, for, do and while statements, even if the body contains only a single statement. Avoid using the following example:

    ```java
    if (condition) statements; 
    ```
* 3 ``[Recommended]`` Do not use complicated expressions in conditional statements (except for frequently used methods like getXxx/isXxx). Using boolean variables to store results of complicated expressions temporarily will increase the code's readability.
Note: Logic within many if statements are very complicated. Readers need to analyze the final results of the conditional expression to understand the branching logic.  
Positive example:

    ```java
    // please refer to the pseudo-code as follows 
    boolean existed = (file.open(fileName, "w") != null) && (...) || (...);
    if (existed) {
        //...
    }  
    ```

    Counter example:  

    ```java
    if ((file.open(fileName, "w") != null) && (...) || (...)) {
       // ...
    }
    ```
* 4 ``[Recommended]`` Avoid using the negation operator '!'.
Note: The negation operator is not easy to be quickly understood. There must be a positive way to represent the same logic.

### <font color="green">Exception</font>
* 1 ``[Mandatory]`` Make sure to invoke the rollback if a method throws an Exception. Rollbacks are based on the context of the coding logic.
* 2 ``[Mandatory]`` Never use return within a finally block. A return statement in a finally block will cause exceptions or result in a discarded return value in the try-catch block.
* 3 ``[Recommended]`` One of the most common errors is NullPointerException. Pay attention to the following situations:
    * 1 If the return type is primitive, return a value of wrapper class may cause NullPointerException.
      Counter example: public int f() { return Integer } Unboxing a null value will throw a NullPointerException.
    * 2 The return value of a database query might be null.
    * 3 Elements in collection may be null, even though Collection.isEmpty() returns false.
    * 4 Return values from an RPC might be null.
    * 5 Data stored in sessions might by null.
    * 6 Method chaining, like obj.getA().getB().getC(), is likely to cause NullPointerException.  
      Positive example: Use Optional to avoid null check and NPE (Java 8+).

### <font color="green">Code Comments</font>
* 1 ``[Mandatory]`` Javadoc should be used for classes, class variables and methods. The format should be '/** comment **/', rather than '// xxx'.  
Note: In IDE, Javadoc can be seen directly when hovering, which is a good way to improve efficiency.
* 2 ``[Mandatory]`` Abstract methods (including methods in interface) should be commented by Javadoc. Javadoc should include method instruction, description of parameters, return values and possible exceptions.
* 3 ``[Mandatory]`` Every class should include information of author(s) and date.
* 4 ``[Mandatory]`` Single line comments in a method should be put above the code to be commented, by using // and multiple lines by using /* */. Alignment for comments should be noticed carefully.
* 5 ``[Mandatory]`` All enumeration type fields should be commented as Javadoc style.
* 6 ``[Recommended]`` Codes or configuration that is noticed to be obsoleted should be resolutely removed from projects.


### <font color="green">Other</font>
* 1``[Mandatory]`` Avoid using *Apache Beanutils* to copy attributes.
* 2 ``[Mandatory]`` When using regex, precompile needs to be done in order to increase the matching performance and preferably stored as a constant.  
Note: Do not define Pattern pattern = Pattern.compile(.); within method body.
* 3 ``[Mandatory]`` Variables must add exclamatory mark when passing to velocity engine from backend, like $!{var}.  
Note: If attribute is null or does not exist, ${var} will be shown directly on web pages.
* 4 ``[Mandatory]`` The return type of Math.random() is double, value range is 0<=x<1 (0 is possible). If a random integer is required, do not multiply x by 10 then round the result. The correct way is to use nextInt or nextLong method which belong to Random Object.
* 5 ``[Mandatory]`` Use System.currentTimeMillis() to get the current millisecond. Do not use new Date().getTime().   
Note: In order to get a more accurate time, use System.nanoTime(). In JDK8, use Instant class to deal with situations like time statistics.
* 6 ``[Mandatory]`` When doing date formatting, "y" should be written in lowercase for "year" in a pattern statement.
Note: When doing date formatting, "yyyy" represents the day in which year, while "YYYY" represents the week in which year (a concept introduced in JDK7). If a week is across two years, the returning "YYYY"represents the next year.
    
    Some more points need to be notices:
     * Uppercase "M" stands for month.
     * Lowercase "m" stands for minute.
     * Uppercase "H" stands for 24-hour clock.
     * Lowercase "h" stands for 12-hour clock.

    Positive Example: Example pattern for date formatting:

    ```java
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ```
    
    Counter Example: Someone applied "YYYY/MM/dd" pattern for date formatting, and the execution result of 2017/12/31 was 2018/12/31, leading to a serious failure.
* 7 ``[Recommended]`` The total number of lines for a method should not be more than 80.
Note: The total number of lines, including the method signature, closing brace, codes, comments, blank lines, line breaks and any invisible lines, should not be more than 80.
