package com.riteny.javalin;

import com.riteny.javalin.common.PathParam;
import com.riteny.javalin.common.RequestBody;
import com.riteny.javalin.common.RequestParam;
import com.riteny.javalin.entity.User;
import com.riteny.javalin.filter.*;
import io.javalin.http.Context;

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
