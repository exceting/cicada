## LogTrace 使用指南
> ⚠️ 建议使用gradle作为项目管理工具
### Chapter 1: 解决的问题
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
### Chapter 2: 导包
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
### Chapter 3: 快速开始
确定jar包和仓库已经配好后开始快速使用：
<br/>首先在测试类上加`@Slf4jCheck`注解
![@Slf4j注解](https://raw.githubusercontent.com/exceting/OSSRH-96790/main/cicada-tools/log-trace/log-trace-01.png)

### 与arthus的区别?
它们不是一个维度的东西，arthus是在运行期改变逻辑或监听执行结果，需要安装、打指令、观察，门槛较高。
<br/>LogTrace更像lombok，都是在编译期就帮你做好所有事情，lombok可以帮你生成set、get方法，LogTrace可以帮你生成业务日志，当然，它 俩配合使用会得到更好的表现:如利用lombok生成toString方法，logTrace打印时就会输出对象的全信息。
