# 项目描述

<p>
	运行环境：JDK17<br>
</p>
<p>
	基于javalin框架进行封装，使用类似spring 注解的方式标注controller，拦截器，websocket和异常处理逻辑
</p>
<p>
	使用javalin作为基底，用于开发轻量级的简单应用，同时使用类似spring的方法方便项目成员快速上手
 </p>

# 使用方式

## 使用注解[RestController](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fapi%2FRestController.java)的方式标注Controller

在类上使用注解[RestController](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fapi%2FRestController.java)
，将该类标注为控制器
<br><br>
在标注为控制器的类下，在方法上使用注解

[GetMapping](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fapi%2FGetMapping.java)

[PostMapping](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fapi%2FPostMapping.java)

[PutMapping](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fapi%2FPutMapping.java)

[DeleteMapping](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fapi%2FDeleteMapping.java)
<br><br>
方法中的参数可以通过注解注入请求参数

使用[PathParam](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FPathParam.java)获取路径传参

使用[RequestBody](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FRequestBody.java)获取request
body的内容

使用[RequestParam](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FRequestParam.java)获取以？方式的传参

样例代码

    @RestController(path = "/test")
    public class SampleController {
    
        @GetMapping(path = "/")
        public void testGet(Context ctx) {
            ctx.result("Hello World");
        }
    
        @PostMapping(path = "/{cid}")
        public void testPost(@RequestParam(name = "name") String name, @PathParam(name = "cid") String cid, @RequestBody User body, JavalinServletContext ctx) {
    
            System.out.println("Cid : " + cid);
            System.out.println("Name : " + name);
            System.out.println("Message : " + body);
    
            ctx.result(ctx.body());
        }
    }

## 使用注解[Filter](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Ffilter%2FFilter.java)的方式标注拦截器

在类上使用注解[Filter](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Ffilter%2FFilter.java)，将该类标注为拦截器
<br><br>
在标注为拦截器的类下，在方法上使用注解

[FilterAfter](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Ffilter%2FFilterAfter.java):
该注解在请求发生时，无论请求地址是否存在，都会调用

[FilterAfterMatched](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Ffilter%2FFilterAfterMatched.java):
该注解在请求发生时，该注解只有在请求地址正确存在的时候会调用

[FilterBefore](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Ffilter%2FFilterBefore.java):
该注解在请求完成后，无论请求地址是否存在，都会调用

[FilterBeforeMatched](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Ffilter%2FFilterBeforeMatched.java)：
该注解在请求完成后， 该注解只有在请求地址正确存在的时候会调用
<br><br>
在拦截器中，方法中的参数同样可以通过注解注入请求参数

使用[PathParam](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FPathParam.java)获取路径传参

使用[RequestBody](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FRequestBody.java)获取request
body的内容

使用[RequestParam](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FRequestParam.java)获取以？方式的传参

    @Filter(filterPath = "/test/*")
    public class SampleFilter {
    
        @FilterBefore
        public void before(@RequestParam(name = "name") String name, @PathParam(name = "cid") String cid, @RequestBody User body, Context ctx) {
            System.out.println("Cid : " + cid);
            System.out.println("Name : " + name);
            System.out.println("Message : " + body);
            System.out.println("/test/Before matched filter ");
        }
    
        @FilterAfter
        public void after(@RequestParam(name = "name") String name, @PathParam(name = "cid") String cid, @RequestBody User body, Context ctx) {
            System.out.println("Cid : " + cid);
            System.out.println("Name : " + name);
            System.out.println("Message : " + body);
            System.out.println("/test/After matched filter ");
        }
    
        @FilterBeforeMatched
        public void beforeMatched(@RequestParam(name = "name") String name, @PathParam(name = "cid") String cid, @RequestBody User body, Context ctx) {
            System.out.println("Cid : " + cid);
            System.out.println("Name : " + name);
            System.out.println("Message : " + body);
            System.out.println("/test/beforeMatched matched filter ");
        }
    
        @FilterAfterMatched
        public void afterMatched(@RequestParam(name = "name") String name, @PathParam(name = "cid") String cid, @RequestBody User body, Context ctx) {
            System.out.println("Cid : " + cid);
            System.out.println("Name : " + name);
            System.out.println("Message : " + body);
            System.out.println("/test/afterMatched matched filter ");
        }
    }

## 使用注解[WebSocketEndpoint](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fws%2FWebSocketEndpoint.java)的方式标注websocket

在类上使用注解[WebSocketEndpoint](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fws%2FWebSocketEndpoint.java)
，将该类标注为WebSocket 处理类
<br><br>
在标注为WebSocket的类下，在方法上使用注解

[OnConnect](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fws%2FOnConnect.java): WebSocket 客户端连接时，调用该方法

[OnClose](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fws%2FOnClose.java): WebSocket 客户端关闭连接时，调用该方法

[OnMessage](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fws%2FOnMessage.java)： 客户端发送消息到服务端时，调用该方法

[OnError](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fws%2FOnError.java)： 连接异常时，调用该方法
<br><br>
在Websocket中，方法中的参数同样可以通过注解注入请求参数

使用[PathParam](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FPathParam.java)获取路径传参

使用[RequestParam](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FRequestParam.java)获取以？方式的传参

其中[RequestBody](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fcommon%2FRequestBody.java)获取request
body的内容只能在[OnMessage](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fws%2FOnMessage.java)的方法中使用

    @WebSocketEndpoint(path = "/ws/test/{cid}", clientId = "cid")
    public class SampleWebSocket {
    
        @OnConnect
        public void onConnect(@PathParam(name = "cid") Integer cid, @RequestParam(name = "name") String name, WsConnectContext ctx) {
            System.out.println("Cid : " + cid);
            System.out.println("Name : " + name);
            System.out.println(ctx.session);
        }
    
        @OnMessage
        public void onMessage(@RequestBody WsMessageContext ctx, @RequestBody User message, @PathParam(name = "cid") Integer cid, @RequestParam(name = "name") String name) {
            System.out.println("Cid : " + cid);
            System.out.println("Name : " + name);
            System.out.println("Message : " + message);
            System.out.println(ctx.session);
        }
    
        @OnBinaryMessage
        public void onBinaryMessage(WsBinaryMessageContext ctx) {
            System.out.println(ctx.pathParam("cid"));
            System.out.println(ctx.session);
        }
    
        @OnError
        public void onError(WsErrorContext ctx) {
            System.out.println(ctx.pathParam("cid"));
            System.out.println(ctx.session);
        }
    
        @OnClose
        public void onClose(WsCloseContext ctx) {
            System.out.println(ctx.pathParam("cid"));
            System.out.println(ctx.session);
        }
    }

## 使用注解[ExceptionController.java](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fexception%2FExceptionController.java)的方式标注异常处理控制器

在类上使用注解[ExceptionController.java](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fexception%2FExceptionController.java)
，将该类标注为异常处理控制器
<br><br>
在标注为异常处理控制器的类下，在方法上使用注解[ExceptionHandlerEndpoint](javalin-package%2Fsrc%2Fmain%2Fjava%2Fcom%2Friteny%2Fjavalin%2Fexception%2FExceptionHandlerEndpoint.java) 并在注解参数中写入需要拦截的异常类型

    public class TestException extends Exception {
        public TestException(String msg) {
            super(msg);
        }
    }

    @ExceptionController
    public class SampleExceptionController {
    
        @ExceptionHandlerEndpoint(exceptionClass = TestException.class)
        public void exception(Exception e, Context ctx) {
            ctx.result("Come to TestException " + e.getMessage());
        }
    }

## 项目启动类配置

    public class Application {
        public static void main(String[] args) {
            ApplicationInit.init(Application.class);
        }
    }

其中Application 配置为程序的入口，并在打包成jar后，生成启动清单，以下为maven使用maven-shade的实例代码

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.4</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <finalName>${project.artifactId}-${project.version}-exec</finalName>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.riteny.Application</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
