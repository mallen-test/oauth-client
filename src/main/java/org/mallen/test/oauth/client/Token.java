package org.mallen.test.oauth.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Token {
    private String accessToken;
    private String tokenType;
    private String refreshToken;

    public static void main(String[] args) {
        Token token = new Token();
        token.setAccessToken("mallen token");

        System.out.println(token);
    }

}