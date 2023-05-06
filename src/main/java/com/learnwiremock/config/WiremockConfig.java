package com.learnwiremock.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WiremockConfig {

    @Value("${movieapp.baseUrl}")
    private String baseUrl;

    @Bean
    public WebClient webClient() {
        HttpClient tcpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> {
                connection.addHandlerFirst(new ReadTimeoutHandler(5))
                    .addHandlerFirst(new WriteTimeoutHandler(5));
            });
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(tcpClient))
            .baseUrl(baseUrl).build();
    }
}
