package io.surisoft.websocker.server;

import io.surisoft.websocker.server.websocket.WebsocketProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class WebsocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);

    @Value("${capi.websocket.server.port}")
    private int capiWebsocketServerPort;

    @Value("${capi.websocket.server.host}")
    private String capiWebsocketServerHost;



    @Autowired
    WebsocketProxy websocketProxy;





    public static void main(String[] args) {
        SpringApplication.run(WebsocketServer.class, args);
    }

    @EventListener
    public void mainServerListener(ApplicationReadyEvent applicationReadyEvent) throws Exception {
        log.info("CAPI Started, starting websocket support on port: {}", capiWebsocketServerPort);
        //new WebsocketProxy(websocketClients(), capiWebsocketServerHost, capiWebsocketServerPort, new WebsocketAuthorization(getJwtProcessor())).runProxy();
        websocketProxy.runProxy();
    }
}
