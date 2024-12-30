package com.primihub.biz.config.client;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class HttpClientConfiguration {

    @Bean(name = "primaryRestTemplate")
    @Primary
    @LoadBalanced
    public RestTemplate initPrimaryRestTemplate() {
        return new RestTemplate();
    }


    @Bean("restHttpRequestFactory")
    @Scope("prototype")
    @ConfigurationProperties(prefix = "rest.template.connection")
    public HttpComponentsClientHttpRequestFactory restHttpRequestFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // 创建一个信任所有证书的 SSLContext
        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build();

        // 创建一个 HttpClient，使用自定义的 SSLContext
        CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return requestFactory;
    }

    @Bean(name = "soaRestTemplate")
    public RestTemplate initSoaRestTemplate(@Qualifier("restHttpRequestFactory") HttpComponentsClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean(name = "proxyRestTemplate")
    public RestTemplate initproxyRestTemplate(@Qualifier("restHttpRequestFactory") HttpComponentsClientHttpRequestFactory factory) {
        String proxyHost = "118.190.39.100";
        int proxyPort = 30900;
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        HttpClient httpClient = HttpClientBuilder.create().setProxy(proxy).build();
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }

}
