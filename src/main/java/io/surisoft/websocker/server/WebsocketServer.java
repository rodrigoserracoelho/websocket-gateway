package io.surisoft.websocker.server;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.surisoft.websocker.server.cache.WebsocketClient;
import io.surisoft.websocker.server.security.WebsocketAuthorization;
import io.surisoft.websocker.server.websocket.Constants;
import io.surisoft.websocker.server.websocket.WebsocketProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebsocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);

    @Value("${capi.websocket.server.port}")
    private int capiWebsocketServerPort;

    @Value("${capi.websocket.server.host}")
    private String capiWebsocketServerHost;

    @Value("${capi.oidc.provider.host}")
    private String oidcProviderHost;

    @Value("${capi.oidc.provider.realm}")
    private String oidcProviderRealm;

    @Bean
    @ConditionalOnProperty(prefix = "capi.oidc.provider", name = "enabled", havingValue = "true")
    public DefaultJWTProcessor<SecurityContext> getJwtProcessor() throws IOException, ParseException {
        log.trace("Starting CAPI JWT Processor");
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWKSet jwkSet = JWKSet.load(new URL(oidcProviderHost + oidcProviderRealm + Constants.CERTS_URI));
        ImmutableJWKSet<SecurityContext> keySource = new ImmutableJWKSet<>(jwkSet);
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        return jwtProcessor;
    }

    public static void main(String[] args) {
        SpringApplication.run(WebsocketServer.class, args);
    }

    @EventListener
    public void mainServerListener(ApplicationReadyEvent applicationReadyEvent) throws Exception {

        log.info("CAPI Started, starting websocket support on port: {}", capiWebsocketServerPort);

        WebsocketClient echoClient = new WebsocketClient();
        echoClient.setPort(8888);
        echoClient.setHost("http://localhost");
        echoClient.setPath("/echo");

        WebsocketClient testClient = new WebsocketClient();
        testClient.setPort(7777);
        testClient.setHost("http://localhost");
        testClient.setPath("/test");
        testClient.setRequiresSubscription(true);
        testClient.setSubscriptionRole("demo:dev-2");


        Map<String, WebsocketClient> webSocketClientList = new HashMap<>();
        webSocketClientList.put("/echo", echoClient);
        webSocketClientList.put("/test", testClient);

        new WebsocketProxy(webSocketClientList, capiWebsocketServerHost, capiWebsocketServerPort, new WebsocketAuthorization(getJwtProcessor())).runProxy();

    }
}
