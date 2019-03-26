package org.mallen.test.oauth.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author mallen
 * @date 3/15/19
 */
@ConfigurationProperties("mallen.oauth.client")
@Getter
@Setter
public class OAuthClientProperties {
    private String clientId;
    private String clientSecret;
    private String oauthCallback;
    private String scope;
    private String responseType;
    private String grantType;
}
