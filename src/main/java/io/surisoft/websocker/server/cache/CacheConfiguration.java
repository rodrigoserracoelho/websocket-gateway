package io.surisoft.websocker.server.cache;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

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
