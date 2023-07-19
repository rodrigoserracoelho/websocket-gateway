package io.surisoft.websocker.server.websocket;

import io.undertow.util.HttpString;

public class Constants {

    public static final HttpString PROTOCOL_HTTP = new HttpString("HTTP/1.1");
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_QUERY = "access_token";

    public static final String REALMS_CLAIM = "realm_access";
    public static final String ROLES_CLAIM = "roles";

    public static final String CERTS_URI = "/protocol/openid-connect/certs";
}
