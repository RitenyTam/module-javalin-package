package com.riteny.javalin;

import com.riteny.javalin.api.GetMapping;
import com.riteny.javalin.api.PostMapping;
import com.riteny.javalin.api.RestController;
import com.riteny.javalin.common.PathParam;
import com.riteny.javalin.common.RequestBody;
import com.riteny.javalin.common.RequestParam;
import com.riteny.javalin.entity.User;
import io.javalin.http.Context;
import io.javalin.http.servlet.JavalinServletContext;

@RestController(path = "/test2")
public class SampleController {

    @GetMapping(path = "/")
    public User testGet(JavalinServletContext ctx) {
        ctx.result("Hello World");

        User user = new User();
        user.setName("admin");
        user.setPassword("123456");

        return user;
    }

    @PostMapping(path = "/{cid}")
    public String testPost(@RequestParam(name = "name") String name, @PathParam(name = "cid") String cid, @RequestBody User body, JavalinServletContext ctx) {

        System.out.println("Cid : " + cid);
        System.out.println("Name : " + name);
        System.out.println("Message : " + body);

        return ctx.body();
    }

    @PostMapping(path = "/exception")
    public void testException(JavalinServletContext ctx) {
        ctx.status(200);
        throw new ArrayStoreException("Hello World Exception");
    }
}
