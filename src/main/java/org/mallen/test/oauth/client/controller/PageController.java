package org.mallen.test.oauth.client.controller;

import org.mallen.test.oauth.client.OAuthClientProperties;
import org.mallen.test.oauth.client.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @author mallen
 * @date 3/15/19
 */
@Controller
public class PageController {
    private static final Logger logger = LoggerFactory.getLogger(PageController.class);
    @Autowired
    private OAuthClientProperties clientProperties;
    @Value("${mallen.oauth.server.authorizeUrl}")
    private String authorizeUrl;
    @Value("${mallen.oauth.server.tokenUrl}")
    private String tokenUrl;
    private static volatile RestTemplate restTemplate;

    @GetMapping("home")
    public String home() {
        return "home.html";
    }

    /**
     * 开始认证过程
     *
     * @return
     */
    @GetMapping("start")
    public void start(HttpSession session, HttpServletResponse response) {
        // 生成state，防止CSRF（Cross-site request forgery/跨站请求伪造）
        String state = UUID.randomUUID().toString();
        session.setAttribute("state", state);
        // 构造重定向地址
        response.addHeader("location", buildAuthUrl(state));
        response.setStatus(302);
    }

    @GetMapping("oauthCallback")
    public String oauthCallback(HttpSession session, @RequestParam("code") String code, @RequestParam(value = "state", required = false) String state) {
        logger.debug("获取到的code为：{}，获取到的state为：{}", code, state);

        // 判断state，防止CSRF（Cross-site request forgery/跨站请求伪造）
        if (!StringUtils.isEmpty(state)) {
            String originState = (String) session.getAttribute("state");
            if (StringUtils.isEmpty(originState)) {
                logger.error("未从session中获取到state");
            } else if (!state.equals(originState)) {
                logger.error("OAuth服务器传递的state与session中的state不一致，分别为：{}、{}", state, originState);
            }
        }
        // 调用服务器获取token
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        MultiValueMap<String, Object> formParam = new LinkedMultiValueMap<>();
//        formParam.add("clientId", clientProperties.getClientId());
//        formParam.add("clientSecret", clientProperties.getClientSecret());
        formParam.add("code", code);
        formParam.add("grantType", clientProperties.getGrantType());
        HttpEntity httpEntity = new HttpEntity(formParam, httpHeaders);
        Token token = restTemplate().postForObject(tokenUrl, httpEntity, Token.class);
        logger.debug("获取到的token为：{}", token);
        return "loginSuccess.html";
    }

    private String buildAuthUrl(String state) {
        return new StringBuilder(authorizeUrl)
                .append("?clientId=").append(clientProperties.getClientId())
                .append("&responseType=").append(clientProperties.getResponseType())
                .append("&scope=").append(clientProperties.getScope())
                .append("&state=").append(state)
                .append("&redirectUri=").append(clientProperties.getOauthCallback()).toString();
    }

    public RestTemplate restTemplate() {
        if (restTemplate == null) {
            synchronized (PageController.class) {
                if (restTemplate == null) {
                    restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
                    restTemplate.getInterceptors().add(
                            new BasicAuthenticationInterceptor(clientProperties.getClientId(), clientProperties.getClientSecret(), Charset.forName("UTF-8"))
                    );
                }
            }
        }

        return restTemplate;
    }
}
