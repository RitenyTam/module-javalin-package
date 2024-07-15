package com.riteny.javalin;

import com.riteny.javalin.api.*;
import com.riteny.javalin.common.PathParam;
import com.riteny.javalin.common.RequestBody;
import com.riteny.javalin.common.RequestParam;
import com.riteny.javalin.exception.ExceptionController;
import com.riteny.javalin.exception.ExceptionHandlerEndpoint;
import com.riteny.javalin.filter.*;
import com.riteny.javalin.ws.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.util.StringUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicationInit {

    private final static Logger logger = LoggerFactory.getLogger("api");

    private final static Map<Class<?>, ExceptionHandler<Exception>> exceptionHandlers = new HashMap<>();

    public static void init(Class<?> clazz) {

        var app = Javalin.create(config -> {
                    config.staticFiles.add("template");
                    config.requestLogger.http((ctx, aFloat) ->
                            logger.info("Url: {}, Method: {}, Param : {}, Body: {},Result : {}, Use time : {} ms"
                                    , ctx.url(), ctx.method(), ctx.queryParamMap(), ctx.body(), ctx.result(), aFloat));
                })
                .exception(InvocationTargetException.class, (e, ctx) -> {
                    //由於異常處理注解通過反射調用，所以儅業務層方法抛出異常后，首先由反射抛出InvocationTargetException，或導致javalin的攔截機制失效
                    //所以這個位置先攔截反射抛出的異常，再將實際的異常抛出
                    ExceptionHandler<Exception> handler = exceptionHandlers.get(e.getTargetException().getClass());
                    Exception exception = (Exception) e.getTargetException();
                    if (handler != null) {
                        handler.handle(exception, ctx);
                    } else {
                        //沒有配置攔截器的異常，通過運行時異常抛出
                        throw new RuntimeException(e);
                    }
                })
                .start(8080);

        String basePackage = clazz.getPackageName();

        initJavalinFilter(app, basePackage);
        initJavalinController(app, basePackage);
        initJavalinExceptionController(basePackage);
        initJavalinWebSocketEndpoint(app, basePackage);
    }

    private static void initJavalinFilter(Javalin app, String basePackage) {

        //包掃描
        Reflections reflections = new Reflections(basePackage);

        //獲取所有帶有JavalinFilter的類
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Filter.class);

        classes.forEach(clazz -> {

            //獲取JavalinFilter上配置的攔截路徑
            Filter filter = clazz.getAnnotation(Filter.class);

            //獲取第一個構造器，目前暫時只能使用無參構造器
            Constructor<?> defaultConstructor = clazz.getConstructors()[0];

            try {
                Object o = defaultConstructor.newInstance();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    //默認使用了JavalinFilterBeforeMatched注解的方法只會擁有一個context對象
                    if (method.isAnnotationPresent(FilterBefore.class)) {
                        app.beforeMatched(filter.filterPath(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                    if (method.isAnnotationPresent(FilterBeforeMatched.class)) {
                        app.beforeMatched(filter.filterPath(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                    if (method.isAnnotationPresent(FilterAfter.class)) {
                        app.beforeMatched(filter.filterPath(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                    if (method.isAnnotationPresent(FilterAfterMatched.class)) {
                        app.beforeMatched(filter.filterPath(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void initJavalinController(Javalin app, String basePackage) {

        //包掃描
        Reflections reflections = new Reflections(basePackage);

        //獲取所有帶有JavalinFilter的類
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RestController.class);

        classes.forEach(clazz -> {

            //獲取JavalinFilter上配置的攔截路徑
            RestController restController = clazz.getAnnotation(RestController.class);

            //獲取第一個構造器，目前暫時只能使用無參構造器
            Constructor<?> defaultConstructor = clazz.getConstructors()[0];

            try {
                Object o = defaultConstructor.newInstance();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        app.get(restController.path() + getMapping.path(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                    if (method.isAnnotationPresent(PostMapping.class)) {
                        PostMapping postMapping = method.getAnnotation(PostMapping.class);
                        app.post(restController.path() + postMapping.path(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                    if (method.isAnnotationPresent(PutMapping.class)) {
                        PutMapping putMapping = method.getAnnotation(PutMapping.class);
                        app.put(restController.path() + putMapping.path(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                    if (method.isAnnotationPresent(DeleteMapping.class)) {
                        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
                        app.delete(restController.path() + deleteMapping.path(), context -> method.invoke(o, injectHttpContextParams(method, context)));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static <T extends Context> Object[] injectHttpContextParams(Method method, T ctx) {

        Object[] params = new Object[method.getParameterCount()];
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(ctx.getClass())) {
                params[i] = ctx;
            } else if (parameters[i].isAnnotationPresent(PathParam.class)) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                String pathParamValue = ctx.pathParam(pathParam.name());
                params[i] = parseQueryOrPathParam(parameters[i], pathParamValue);

            } else if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
                String requestParamValue = ctx.queryParam(requestParam.name());
                params[i] = parseQueryOrPathParam(parameters[i], requestParamValue);

            } else if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                params[i] = parseRequestBody(parameters[i], ctx);
            } else {
                params[i] = null;
            }
        }

        return params;
    }

    private static Object parseRequestBody(Parameter parameter, Context ctx) {

        if (parameter.getType().equals(String.class)) {
            return ctx.body();
        } else if (parameter.getType().equals(Integer.class)) {
            return Integer.parseInt(ctx.body());
        } else if (parameter.getType().equals(Double.class)) {
            return Double.parseDouble(ctx.body());
        } else if (parameter.getType().equals(Long.class)) {
            return Long.parseLong(ctx.body());
        } else if (parameter.getType().equals(Boolean.class)) {
            return Boolean.parseBoolean(ctx.body());
        } else if (parameter.getType().equals(Float.class)) {
            return Float.parseFloat(ctx.body());
        } else {
            return ctx.bodyAsClass(parameter.getType());
        }
    }


    private static void initJavalinExceptionController(String basePackage) {

        //包掃描
        Reflections reflections = new Reflections(basePackage);

        //獲取所有帶有JavalinFilter的類
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(ExceptionController.class);

        classes.forEach(clazz -> {

            //獲取第一個構造器，目前暫時只能使用無參構造器
            Constructor<?> defaultConstructor = clazz.getConstructors()[0];

            try {
                Object o = defaultConstructor.newInstance();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(ExceptionHandlerEndpoint.class)) {

                        ExceptionHandlerEndpoint exceptionHandlerEndpoint = method.getAnnotation(ExceptionHandlerEndpoint.class);

                        ExceptionHandler<Exception> exceptionHandler = (e, context) -> {
                            try {
                                method.invoke(o, e, context);
                            } catch (IllegalAccessException | InvocationTargetException ex) {
                                throw new RuntimeException(ex);
                            }
                        };

                        exceptionHandlers.put(exceptionHandlerEndpoint.exceptionClass(), exceptionHandler);
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void initJavalinWebSocketEndpoint(Javalin app, String basePackage) {

        //包掃描
        Reflections reflections = new Reflections(basePackage);

        //獲取所有帶有JavalinFilter的類
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(WebSocketEndpoint.class);

        classes.forEach(clazz -> {

            //獲取JavalinFilter上配置的攔截路徑
            WebSocketEndpoint webSocketEndpoint = clazz.getAnnotation(WebSocketEndpoint.class);

            //獲取第一個構造器，目前暫時只能使用無參構造器
            Constructor<?> defaultConstructor = clazz.getConstructors()[0];

            try {
                Object o = defaultConstructor.newInstance();
                Method[] methods = clazz.getDeclaredMethods();

                app.ws(webSocketEndpoint.path(), ws -> {
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(OnConnect.class)) {
                            ws.onConnect(ctx -> method.invoke(o, injectWsContextParams(method, ctx)));
                        }
                        if (method.isAnnotationPresent(OnMessage.class)) {
                            ws.onMessage(ctx -> method.invoke(o, injectWsContextParams(method, ctx)));
                        }
                        if (method.isAnnotationPresent(OnError.class)) {
                            ws.onError(ctx -> method.invoke(o, injectWsContextParams(method, ctx)));
                        }
                        if (method.isAnnotationPresent(OnBinaryMessage.class)) {
                            ws.onBinaryMessage(ctx -> method.invoke(o, injectWsContextParams(method, ctx)));
                        }
                        if (method.isAnnotationPresent(OnClose.class)) {
                            ws.onClose(ctx -> method.invoke(o, injectWsContextParams(method, ctx)));
                        }
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static <T extends WsContext> Object[] injectWsContextParams(Method method, T ctx) {

        Object[] params = new Object[method.getParameterCount()];
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(ctx.getClass())) {
                params[i] = ctx;
            } else if (parameters[i].isAnnotationPresent(PathParam.class)) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                String pathParamValue = ctx.pathParam(pathParam.name());
                params[i] = parseQueryOrPathParam(parameters[i], pathParamValue);

            } else if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
                String requestParamValue = ctx.queryParam(requestParam.name());
                params[i] = parseQueryOrPathParam(parameters[i], requestParamValue);

            } else if (parameters[i].isAnnotationPresent(RequestBody.class) && ctx.getClass().equals(WsMessageContext.class)) {
                params[i] = parseRequestBody(parameters[i], (WsMessageContext) ctx);
            } else {
                params[i] = null;
            }
        }

        return params;
    }

    private static Object parseRequestBody(Parameter parameter, WsMessageContext ctx) {

        if (parameter.getType().equals(String.class)) {
            return ctx.message();
        } else if (parameter.getType().equals(Integer.class)) {
            return Integer.parseInt(ctx.message());
        } else if (parameter.getType().equals(Double.class)) {
            return Double.parseDouble(ctx.message());
        } else if (parameter.getType().equals(Long.class)) {
            return Long.parseLong(ctx.message());
        } else if (parameter.getType().equals(Boolean.class)) {
            return Boolean.parseBoolean(ctx.message());
        } else if (parameter.getType().equals(Float.class)) {
            return Float.parseFloat(ctx.message());
        } else {
            return ctx.messageAsClass(parameter.getType());
        }
    }

    private static Object parseQueryOrPathParam(Parameter parameter, String paramValue) {

        if (StringUtil.isEmpty(paramValue)) {
            return null;
        }

        if (parameter.getType().equals(String.class)) {
            return paramValue;
        } else if (parameter.getType().equals(Integer.class)) {
            return Integer.parseInt(paramValue);
        } else if (parameter.getType().equals(Double.class)) {
            return Double.parseDouble(paramValue);
        } else if (parameter.getType().equals(Long.class)) {
            return Long.parseLong(paramValue);
        } else if (parameter.getType().equals(Boolean.class)) {
            return Boolean.parseBoolean(paramValue);
        } else if (parameter.getType().equals(Float.class)) {
            return Float.parseFloat(paramValue);
        } else {
            return null;
        }
    }
}
