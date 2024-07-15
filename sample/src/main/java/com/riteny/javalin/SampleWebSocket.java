package com.riteny.javalin;

import com.riteny.javalin.common.PathParam;
import com.riteny.javalin.common.RequestBody;
import com.riteny.javalin.common.RequestParam;
import com.riteny.javalin.entity.User;
import com.riteny.javalin.ws.*;
import io.javalin.websocket.*;

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
