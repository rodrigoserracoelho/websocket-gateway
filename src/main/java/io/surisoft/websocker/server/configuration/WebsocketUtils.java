package io.surisoft.websocker.server.configuration;

import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.surisoft.websocker.server.security.WebsocketAuthorization;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class WebsocketUtils {
    @Autowired(required = false)
    private DefaultJWTProcessor defaultJWTProcessor;

    public HttpHandler createClientHttpHandler(WebsocketClient webSocketClient) {
        LoadBalancingProxyClient loadBalancingProxyClient = new LoadBalancingProxyClient();
        loadBalancingProxyClient.addHost(URI.create(webSocketClient.getHost() + ":" + webSocketClient.getPort()));
        return ProxyHandler
                .builder()
                .setProxyClient(loadBalancingProxyClient)
                .setMaxRequestTime(30000)
                .setNext(ResponseCodeHandler.HANDLE_404)
                .build();
    }

    public WebsocketAuthorization createWebsocketAuthorization() throws CapiWebsocketException {
        if(defaultJWTProcessor != null) {
            return new WebsocketAuthorization(defaultJWTProcessor);
        }
        throw new CapiWebsocketException("No OIDC provider enabled, consider enabling OIDC");
    }
}
