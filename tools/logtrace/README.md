## LogTrace 使用指南
> ⚠️ 建议使用gradle作为项目管理工具
### Part1: 解决的问题
本产品尝试解决以下场景的问题：一块依赖了许多上下游服务的代码，且上下游的返回决定了它的逻辑走向，其中弯弯绕绕的if-else一大堆，除了没写注释外，还没有打印任何日志...
```java
public Student complexScene(String... args) {
    if(args == null || args.length == 0) {
        return null;
    }

    int aResult = aService.getA(args[0]); //假设getA底层是通过数据库拿到的结果
    if(aResult > 5){
        return null;
    }

    List<String> bResults = bService.getBs(args[0]); //假设getBs是通过一个rpc服务拿到的结果
    if(bResults != null && bResults.size() > 0){
        Student student = new Student();
        student.setAge(aResult);
        student.setName(bResults.get(0));
        return student;
    }
    return null;
}
```
以上述代码为例，它有3种逻辑走向会返回null，假如现在这块逻辑在生产环境突然返回null，不符合预期，需要排查问题，你会怎么做? 

此刻你或许会想到利用「可观测系统」进行一系列分析，最终得出结论，但很遗憾，这只适用于上下游服务异常的情况(IO错误)，像上面这种由对方返回了不符合预期的数据导致的问题是无法排查的。

解决这类问题，最直接的方式就是像这样给每个影响逻辑走势的地方打上追踪日志：
```java
public Student complexScene(String... args) {
    if(args == null || args.length == 0) {
        log.debug("args == null or length == 0 is true! args={}", args); //逻辑追踪日志
        return null;
    }

    int aResult = aService.getA(args[0]);
    if(aResult > 5){
        log.debug("aResult > 5 is true! aResult={}", aResult); //逻辑追踪日志
        return null;
    }

    List<String> bResults = bService.getBs(args[0]);
    log.debug("bResults={}", bResults); //逻辑追踪日志
    if(bResults != null && bResults.size() > 0){
        Student student = new Student();
        student.setAge(aResult);
        student.setName(bResults.get(0));
        return student;
    }
    return null;
}
```
这样不管从哪里返回null，都可以通过日志分析出逻辑走向。

这只是个简单的例子，在实际开发中往往有巨复杂的逻辑，最典型的就是网关接口，内部可能聚合了高达十几个rpc服务的返回值，中间产生的条件判断逻辑更是数不胜数，
像这种场景一旦返回了不符合预期的结果，如果没有追踪日志排查起来将会极其痛苦。

虽然通过追踪日志很容易排查出问题所在，但打印这些日志是麻烦的，你要考虑在哪里打，输出哪些数据，格式应该怎样，如何避免打印无意义的日志。
<br/>LogTrace就是用来解决这种问题的，它会`自动解析语法树`，在会影响逻辑走向的地方`自动植入`风格统一的追踪日志，下面来看看它具体的用法。
### Part2: 导包
LogTrace可以自动附加`有意义`且`风格统一`的业务追踪日志，而且它像lombok一样简单易用。

首先将下面的jar包导入到你的项目中
> ⚠️ 注意: 
> <br/>slf4j和logback是必须的，如果你项目中已经引入了，就不用再引了
> <br/>除了logback，引入别的slf4j标准实现也可以，如log4j

maven：
```xml
<dependencies>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.7</version>
    </dependency>
    <dependency>
    <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.9</version>
    </dependency>
    <dependency>
        <groupId>io.github.exceting</groupId>
        <artifactId>log-trace</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <annotationProcessors>
                    <annotationProcessor>io.github.exceting.cicada.tools.logtrace.LogTraceProcessor</annotationProcessor>
                </annotationProcessors>
            </configuration>
        </plugin>
    </plugins>
</build>
```
gradle：
```groovy
compileOnly 'io.github.exceting:log-trace:0.0.1-SNAPSHOT'
annotationProcessor 'io.github.exceting:log-trace:0.0.1-SNAPSHOT'
```
如你所见，现在的包是snapshot版本，所以要把sonatype的snapshot仓库依赖加进来：
<br/>
maven：
```xml
<repository>
    <id>snapshots</id>
    <name>sonatype snapshot</name>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
</repository>
```
gradle：
```groovy
//放到repositories里面
maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots/" }
```
### Part3: 快速开始
确定jar包和仓库已经配好后开始快速使用，首先在测试类上加`@Slf4jCheck`注解
![@Slf4j注解](https://raw.githubusercontent.com/exceting/OSSRH-96790/main/cicada-tools/log-trace/log-trace-01.png)

然后在需要被追踪的方法上加@MethodLog注解:
![@MethodLog注解](https://raw.githubusercontent.com/exceting/OSSRH-96790/main/cicada-tools/log-trace/log-trace-02.png)
运行结果如图。
### Part4: 格式&基本原理
通过LogTrace植入的追踪日志统一格式如下：
![日志格式](https://raw.githubusercontent.com/exceting/OSSRH-96790/main/cicada-tools/log-trace/log-trace-03.png)
LogTrace的工作原理与lombok一致，都是在编译期解析语法树，通过对应的注解增强原有代码，即在编译器修改源代码的方式实现，
[参考这里](https://exceting.github.io/2021/02/08/%E6%8F%92%E5%85%A5%E5%BC%8F%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8%E7%9A%84%E4%B8%80%E6%AC%A1%E4%BD%BF%E7%94%A8%E7%BB%8F%E5%8E%86/)
### Part5: 注解&用法
#### @Slf4jCheck
每个需要打追踪日志的类上都应该加上这个注解，加上此注解后，类内会自动创建一个Slf4j的Logger对象，作用等同于lombok的`@Slf4j`且兼容lombok。
<br/>
它有一个属性：
* isOpen：用来控制是否输出追踪日志，默认为空（输出），支持定制AtomicBoolean开关，灵活控制是否输出日志，对全局方法生效，开关这块内容会放到自定义开关小节详细介绍，这里不再赘述。
#### @MethodLog
除了要在类上加`@Slf4jCheck`，还要在每个需要植入追踪日志的方法上加上`@MethodLog`注解，程序运行起来后，只会给加了此注解的方法植入追踪日志。
<br/>
它有6个属性：
* isOpen：默认为空，作用跟`@Slf4jCheck`里的isOpen一样，但优先级更高，仅对当前方法生效。
* traceLevel：默认为Level.DEBUG，可通过此项定制追踪日志的级别。
* exceptionLog：是否打印方法异常信息，为true时开启，默认false，它的增强效果如下：
  * ```java
    // 编译前
    @MethodLog(exceptionLog = true)
    void methodTest() {
        // 方法体省略...
    }
    
    // ⬇⬇
    
    // 编译后被LogTrace增强后的代码
    void methodTest() {
        try {
            // 方法体省略...
        } catch(Exception e) { // try-catch
            log.error("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][TRY][LINE: 5] Error! Data: ", e); // 输出错误日志（注：异常日志的级别强制为error）
            throw e;
        }
    }
    ```
* noThrow：需要和`exceptionLog`搭配使用，当它的值为true时，则只catch异常，不抛出异常，默认false，它的增强效果如下：
  * ```java
    // 编译前
    @MethodLog(exceptionLog = true, noThrow = true)
    void methodTest() {
        // 方法体省略...
    }
    
    // ⬇⬇
    
    // 编译后被LogTrace增强后的代码
    void methodTest() {
      try {
        // 方法体省略...
      } catch(Exception e) { //仅输出日志，不再throw异常
        log.error("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][TRY][LINE: 5] Error! Data: ", e);
      }
    }
    ```
* dur：是否打印方法耗时？为true时开启，默认false，开启后的增强逻辑如下：
  * ```java
    // 编译前
    @methodTest(dur = true)
    void methodTest() {
      // 方法体省略...
    }
    
    // ⬇⬇
    
    // 编译后被LogTrace增强后的代码
    void methodTest() {
      // 植入的计数变量会加个UUID后缀，防止局部变量冲突
      long start_${UUID} = System.nanoTime();
      try{
        // 方法体省略...
      } finally { //打印出本方法执行耗时
        log.debug("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][TRY][LINE: 5] Finished! Data: duration = {}", (System.nanoTime()-start_${UUID})/1000000L);
      }
    }
    ```
* onlyVar：是否只打印变量追踪日志？默认false，为false时，那些加了`@MethodLog`的方法，会在所有**影响逻辑走势**的地方都加上追踪日志(即方法内任意地方的任意if、if-else，switch-case语句)，增强效果如下：
  * ```java
    // 编译前
    @MethodLog
    void methodTest() {
      int a = 2;
      if(a == 2) {
        // 条件1命中
      } else {
        // 条件2命中
    }
    
    // ⬇⬇
    
    // 编译后被LogTrace增强后的代码
    void methodTest() {
        int a = 2;
        if(a == 2) { // 这里会植入各条件命中时的追踪日志
            log.debug("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][IF][LINE: 29] The condition: (a == 2) is true!");
            // 条件1命中
        } else {
            log.debug("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][IF][LINE: 31] The condition: else is true!");
            // 条件2命中
        }
    }
    ```
    如果onlyVar为true，这些日志将不再打印，这时就只会打印方法体中被`@VarLog`标注的局部变量日志(@VarLog后面会介绍)。<br/>如果你认为不需要那么详细的追踪日志，可以利用此项放弃这些日志。
#### @VarLog
对于方法体中局部变量的追踪，如果你要对方法体中某个局部变量感兴趣，可以在其声明的位置打上这个注解，之后这个变量的值会被追踪，增强过程如下：
```java
// 编译前
@MethodLog
void methodTest() {
    @VarLog //利用@VarLog追踪局部变量a
    int a = getA(); //假设getA是调用另一个RPC服务来拿a的值
    a=5;
}

// ⬇⬇

// 编译后被LogTrace增强后的代码
void methodTest() {
    int a = getA(); //⬇追踪后会打印a的值
    log.debug("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][VARIABLE][LINE: 28]  Data: a = {}", new Object[]{Integer.valueOf(a)});
    a = 5; //⬇之后局部变量在程序中的任意位置被重新赋值，都会将其新值打印出来
    log.debug("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][VARIABLE][LINE: 31]  Data: a = {}", new Object[]{Integer.valueOf(a)});
}
```
除此之外，这个注解还包含一个dur属性，默认值为false，当设置为true时，会打印获取这个变量所消耗的时间。<br/>
对于复杂场景，你可以利用这个注解灵活的追踪任意变量，记录变量被赋予的所有值。
#### @Ban
所有追踪日志在打印时，会无脑打印方法的入参，如果你不需要某个参数被打印，就给它加上这个注解：
```java
// 编译前
@MethodLog
void methodTest(int a, @Ban int b, int c) { //禁止打印参数b
    if(a == 1){
        //业务代码省略...
    }
}

// ⬇⬇

// 编译后被LogTrace增强后的代码
void methodTest(int a, int b, int c) {
    Object final_c = c;
    Object final_a = a;
    if (a == 1) { //⬇在植入的追踪日志中，打印入参时，只打印a和c，被@Ban修饰的b则不打印
        log.debug("LOG_TRACE >>>>>> OUTPUT: [METHOD: methodTest][IF][LINE: 30] The condition: (a == 1) is true! Data: a = {}, c = {}", new Object[]{final_a, final_c});
    }
}
```
### Part6: 自定义开关
非常自由的开关定制方式，在@Slf4jCheck和@MethodLog里面通过isOpen控制日志是否输出，默认输出，MethodLog优先级更高。

定制开关: 在任意类里定义一个开关，这个开关必须是static、final的AtomicBoolean对象：
```java
public static final AtomicBoolean isOpen = new AtomicBoolean(true)
```
利用`全限定名#常量名`的方式引入给isOpen属性：
```java
@Slf4jCheck(isOpen = "io.cicada.mock.tools.config.Test#isOpen")
@MethodLog(isOpen = "io.cicada.mock.tools.config.Test#isOpen")
```
剩下的事情就很简单了，你可以写个定时器，定时从配置系统中获取具体的开关值，来刷新这个对象的值(只刷值，千万不要改引用指针!!)，从而控制日志的是否输出：
```java
@Component
public class OffOnTest {
    
    // 开关
    public static final AtomicBoolean isOpen = new AtomicBoolean(false);
    
    ScheduledExecutorService refreshTask = ThreadPools.newScheduledThreadPool("RefreshSwitch", 1);
    @PostConstruct
    private void init() {
        refreshTask.scheduleWithFixedDelay(() -> {
            // 定时刷新开关值，具体从哪里获取开关状态，取决于你自己的需求，最典型的就是拉取远程配置系统里的值，这样你只需要更新配置系统里的配置，就能控制追踪日志是否打印
            isOpen.set(当前开关值);
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }
}
```
一些C端服务流量较高，如果担心日志的上报对性能有影响，可以通过开关来控制是否输出追踪日志。
### Part7: lombok可以让它更好的工作
利用lombok生成对象的toString方法，将对象整个打印出来:
![日志格式](https://raw.githubusercontent.com/exceting/OSSRH-96790/main/cicada-tools/log-trace/log-trace-04.png)
### Part8: 常见问题
使用maven运行时可能出现类型转换错误异常：
```java
java: java.lang.ClassCastException: class com.sun.proxy.$Proxy15 cannot be cast to class com.sun.tools.javac.processing.JavacProcessingEnvironment (com.sun.proxy.$Proxy15 is in unnamed module of loader java.net.URLClassLoader @59690aa4; com.sun.tools.javac.processing.JavacProcessingEnvironment is in module jdk.compiler of loader 'app')
```
解决：点开IDEA的settings选项，在弹出窗口找到如下位置
![日志格式](https://raw.githubusercontent.com/exceting/OSSRH-96790/main/cicada-tools/log-trace/log-trace-05.png)

将`-Djps.track.ap.dependencies`填入上图指定位置。
### 为什么不用Arthas排查这类问题?
Arthas和LogTrace不是一个维度的东西，Arthas是在运行期改变原有逻辑并监听执行结果，需要安装、打指令、观察等一系列操作，使用门槛较高；
<br/>LogTrace更像lombok，简单易用，在编译期就帮你做好所有事情，lombok可以帮你生成set、get方法，LogTrace可以帮你生成业务日志，当然，它们配合使用会得到更好的表现: 如利用lombok生成toString方法，logTrace打印时就会输出对象的全信息。
