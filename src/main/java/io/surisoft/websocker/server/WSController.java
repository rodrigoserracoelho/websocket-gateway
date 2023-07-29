package io.surisoft.websocker.server;

import io.surisoft.websocker.server.configuration.WebsocketClient;
import io.surisoft.websocker.server.websocket.WebsocketProxy;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
public class WSController {

    @Autowired
    Map<String, WebsocketClient> websocketClientMap;

    @Autowired
    WebsocketProxy proxy;

    @PostMapping("/new")
    public ResponseEntity<WebsocketClient> newClient(@RequestBody WebsocketClient websocketClient) {
        websocketClient.setHttpHandler(createClientHttpHandler(websocketClient));
        if(!websocketClientMap.containsKey(websocketClient.getPath())) {
            websocketClientMap.put(websocketClient.getPath(), websocketClient);
            return new ResponseEntity<>(websocketClient, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> remove(@RequestBody WebsocketClient websocketClient) {
        if(websocketClientMap.containsKey(websocketClient.getPath())) {
            websocketClient.setHttpHandler(null);
            websocketClientMap.remove(websocketClient.getPath());
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    private HttpHandler createClientHttpHandler(WebsocketClient webSocketClient) {
        LoadBalancingProxyClient loadBalancingProxyClient = new LoadBalancingProxyClient();
        loadBalancingProxyClient.addHost(URI.create(webSocketClient.getHost() + ":" + webSocketClient.getPort()));
        return ProxyHandler
                .builder()
                .setProxyClient(loadBalancingProxyClient)
                .setMaxRequestTime(30000)
                .setNext(ResponseCodeHandler.HANDLE_404)
                .build();
    }
}
