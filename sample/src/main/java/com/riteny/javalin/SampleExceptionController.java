package com.riteny.javalin;

import com.riteny.javalin.exception.ExceptionController;
import com.riteny.javalin.exception.ExceptionHandlerEndpoint;
import io.javalin.http.Context;

@ExceptionController
public class SampleExceptionController {

    @ExceptionHandlerEndpoint(exceptionClass = TestException.class)
    public void exception(Exception e, Context ctx) {
        ctx.result("Come to TestException " + e.getMessage());
    }

    @ExceptionHandlerEndpoint(exceptionClass = ArrayStoreException.class)
    public void exception(ArrayStoreException e, Context ctx) {
        ctx.result("Come to ArrayStoreException " + e.getMessage());
    }

}
