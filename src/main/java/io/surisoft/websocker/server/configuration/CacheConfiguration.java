package io.surisoft.websocker.server.configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.surisoft.websocker.server.websocket.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    @Value("${capi.oidc.provider.host}")
    private String oidcProviderHost;

    @Value("${capi.oidc.provider.realm}")
    private String oidcProviderRealm;

    @Bean
    public Map<String, WebsocketClient> websocketClients() {
        /*WebsocketClient echoClient = new WebsocketClient();
        echoClient.setPort(8888);
        echoClient.setHost("http://localhost");
        echoClient.setPath("/echo");*/

        WebsocketClient testClient = new WebsocketClient();
        testClient.setPort(7777);
        testClient.setHost("http://localhost");
        testClient.setPath("/test");
        //testClient.setRequiresSubscription(true);
        //testClient.setSubscriptionRole("demo:dev-2");

        Map<String, WebsocketClient> webSocketClientList = new HashMap<>();
        //webSocketClientList.put("/echo", echoClient);
        webSocketClientList.put("/test", testClient);

        return webSocketClientList;
    }

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

    /*@Bean
    public Cache<String, WebsocketClient> wsClientCache() {
        return new Cache2kBuilder<String, WebsocketClient>(){}
                .name("wsClientCache-" + hashCode())
                .eternal(true)
                .entryCapacity(10000)
                .storeByReference(true)
                .build();
    }*/
}
