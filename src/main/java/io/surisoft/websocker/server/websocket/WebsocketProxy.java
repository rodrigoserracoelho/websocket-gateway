package io.surisoft.websocker.server.websocket;

import io.surisoft.websocker.server.configuration.CapiWebsocketException;
import io.surisoft.websocker.server.configuration.WebsocketClient;
import io.surisoft.websocker.server.configuration.WebsocketUtils;
import io.surisoft.websocker.server.security.WebsocketAuthorization;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "capi.websocket", name = "enabled", havingValue = "true")
public class WebsocketProxy {
    private static final Logger log = LoggerFactory.getLogger(WebsocketProxy.class);
    private final int port = 8381;
    private final String host = "localhost";
    @Autowired
    private Map<String, WebsocketClient> webSocketClients;
    private WebsocketAuthorization websocketAuthorization;
    @Autowired
    private WebsocketUtils websocketUtils;
    private Undertow undertow;


    public void runProxy() {

        try {
            websocketAuthorization = websocketUtils.createWebsocketAuthorization();
        } catch (CapiWebsocketException e) {
            log.warn(e.getMessage());
        }

        webSocketClients.forEach((key, value) -> {
            value.setHttpHandler(websocketUtils.createClientHttpHandler(value));
        });

        this.undertow = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(httpServerExchange -> {
                    String requestPath = httpServerExchange.getRequestPath();
                    if(webSocketClients.containsKey(requestPath)) {
                        if(httpServerExchange.getProtocol().equals(Constants.PROTOCOL_HTTP)) {
                            if(websocketAuthorization != null) {
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
                    }
                }).build();
        undertow.start();
    }


}