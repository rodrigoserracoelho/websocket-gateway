package io.surisoft.websocker.server.websocket;

import io.surisoft.websocker.server.cache.WebsocketClient;
import io.surisoft.websocker.server.security.WebsocketAuthorization;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class WebsocketProxy {
    private static final Logger log = LoggerFactory.getLogger(WebsocketProxy.class);
    private final int port;
    private final String host;
    private final Map<String, WebsocketClient> webSocketClients;
    private final WebsocketAuthorization websocketAuthorization;

    public WebsocketProxy(Map<String, WebsocketClient> webSocketClients, String host, int port, WebsocketAuthorization websocketAuthorization) {
        this.port = port;
        this.host = host;
        this.webSocketClients = webSocketClients;
        this.websocketAuthorization = websocketAuthorization;
    }

    public void runProxy() {
        webSocketClients.forEach((key, value) -> {
            value.setHttpHandler(createClientHttpHandler(value));
        });

        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(httpServerExchange -> {
                    String requestPath = httpServerExchange.getRequestPath();
                    if(webSocketClients.containsKey(requestPath)) {
                        if(httpServerExchange.getProtocol().equals(Constants.PROTOCOL_HTTP)) {
                            if(websocketAuthorization.isAuthorized(webSocketClients.get(requestPath), httpServerExchange)) {
                                log.info("{} is authorized!", httpServerExchange.getRequestPath());
                                httpServerExchange.setRelativePath(httpServerExchange.getRequestPath());
                                webSocketClients.get(requestPath).getHttpHandler().handleRequest(httpServerExchange);
                            } else {
                                log.info("{} is not authorized!", httpServerExchange.getRequestPath());
                                httpServerExchange.setStatusCode(403);
                                httpServerExchange.endExchange();
                                webSocketClients.get(requestPath).getHttpHandler().handleRequest(httpServerExchange);
                            }
                        }
                    }
                }).build();
        server.start();
    }

    private HttpHandler createClientHttpHandler(WebsocketClient webSocketClient) {
        LoadBalancingProxyClient  loadBalancingProxyClient = new LoadBalancingProxyClient();
        loadBalancingProxyClient.addHost(URI.create(webSocketClient.getHost() + ":" + webSocketClient.getPort()));
        return ProxyHandler
                .builder()
                .setProxyClient(loadBalancingProxyClient)
                .setMaxRequestTime(30000)
                .setNext(ResponseCodeHandler.HANDLE_404)
                .build();
    }
}