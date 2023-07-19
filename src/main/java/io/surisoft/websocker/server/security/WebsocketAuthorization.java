package io.surisoft.websocker.server.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.surisoft.websocker.server.cache.WebsocketClient;
import io.surisoft.websocker.server.websocket.Constants;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class WebsocketAuthorization {

    public WebsocketAuthorization(DefaultJWTProcessor defaultJWTProcessor) {
        this.jwtProcessor = defaultJWTProcessor;
    }

    private static final Logger log = LoggerFactory.getLogger(WebsocketAuthorization.class);

    private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

    public boolean isAuthorized(WebsocketClient websocketClient, HttpServerExchange httpServerExchange) {
        if(!websocketClient.requiresSubscription()) {
            return true;
        }
        if(httpServerExchange.getRequestHeaders().contains(Constants.AUTHORIZATION_HEADER)
                || httpServerExchange.getQueryParameters().containsKey(Constants.AUTHORIZATION_QUERY)) {
            return isApiSubscribed(httpServerExchange, websocketClient.getSubscriptionRole());
        }
        return false;
    }

    public String getBearerTokenFromHeader(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    private boolean isApiSubscribed(HttpServerExchange httpServerExchange, String role) {
        String bearerToken;
        if(httpServerExchange.getRequestHeaders().contains(Constants.AUTHORIZATION_HEADER)) {
            bearerToken = getBearerTokenFromHeader(httpServerExchange.getRequestHeaders().get(Constants.AUTHORIZATION_HEADER, 0));
            httpServerExchange.getRequestHeaders().remove(new HttpString(Constants.AUTHORIZATION_HEADER));
        } else {
            bearerToken = httpServerExchange.getQueryParameters().get(Constants.AUTHORIZATION_QUERY).getFirst();
            removeAuthorizationFromQuery(httpServerExchange);
        }
        try {
            JWTClaimsSet jwtClaimsSet = jwtProcessor.process(bearerToken, null);
            Map<String, Object> claimSetMap = jwtClaimsSet.getJSONObjectClaim(Constants.REALMS_CLAIM);
            if(claimSetMap != null && claimSetMap.containsKey(Constants.ROLES_CLAIM)) {
                List<String> roleList = (List<String>) claimSetMap.get(Constants.ROLES_CLAIM);
                for(String claimRole : roleList) {
                    if(claimRole.equals(role)) {
                        return true;
                    }
                }
            }
        } catch (BadJOSEException | JOSEException | ParseException e) {
            log.warn(e.getMessage(), e);
        }
        return false;
    }

    private void removeAuthorizationFromQuery(HttpServerExchange httpServerExchange) {
        StringBuilder queryString = new StringBuilder();
        httpServerExchange.getQueryParameters().forEach((key, value) -> {
            if(!key.equals(Constants.AUTHORIZATION_QUERY)) {
                if(queryString.isEmpty()) {
                    queryString
                            .append(key)
                            .append("=")
                            .append(value.getFirst());
                } else {
                    queryString
                            .append("&")
                            .append(key)
                            .append("=")
                            .append(value.getFirst());
                }
            }
        });
        httpServerExchange.getQueryParameters().clear();
        httpServerExchange.setQueryString(queryString.toString());
    }
}