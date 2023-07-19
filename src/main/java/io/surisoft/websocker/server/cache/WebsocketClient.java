package io.surisoft.websocker.server.cache;

import io.undertow.server.HttpHandler;

public class WebsocketClient {
    private String host;
    private int port;
    private String path;
    private HttpHandler httpHandler;

    private boolean requiresSubscription;
    private String subscriptionRole;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpHandler getHttpHandler() {
        return httpHandler;
    }

    public void setHttpHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public boolean requiresSubscription() {
        return requiresSubscription;
    }

    public void setRequiresSubscription(boolean requiresSubscription) {
        this.requiresSubscription = requiresSubscription;
    }

    public String getSubscriptionRole() {
        return subscriptionRole;
    }

    public void setSubscriptionRole(String subscriptionRole) {
        this.subscriptionRole = subscriptionRole;
    }

}
