/*
 * Copyright 2013-2014 Donghwan Kim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atmosphere.reactio.vertx;

import org.atmosphere.reactio.AbstractServerWebSocket;
import org.atmosphere.reactio.Data;
import org.atmosphere.reactio.ServerWebSocket;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;

/**
 * {@link ServerWebSocket} for Vert.x 2.
 *
 * @author Donghwan Kim
 */
public class VertxServerWebSocket extends AbstractServerWebSocket {

    private final org.vertx.java.core.http.ServerWebSocket socket;
    private String uri;

    public VertxServerWebSocket(org.vertx.java.core.http.ServerWebSocket socket) {
        this.socket = socket;
        socket.closeHandler(new VoidHandler() {
            @Override
            protected void handle() {
                closeActions.fire();
            }
        })
                .exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable throwable) {
                        errorActions.fire(throwable);
                    }
                })
                .dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        messageActions.fire(new Data(buffer.toString()));
                    }
                });
    }

    @Override
    public String uri() {
        if (uri == null) {
            uri = socket.path();
            if (socket.query() != null) {
                uri += "?" + socket.query();
            }
        }
        return uri;
    }

    @Override
    protected void doClose() {
        socket.close();
    }

    @Override
    protected void doSend(String data) {
        socket.writeTextFrame(data);
    }

    /**
     * {@link org.vertx.java.core.http.ServerWebSocket} is available.
     */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        return org.vertx.java.core.http.ServerWebSocket.class.isAssignableFrom(clazz) ?
                clazz.cast(socket) :
                null;
    }

}
