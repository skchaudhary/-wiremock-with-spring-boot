package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWireMock(port = 8090)
@TestPropertySource(properties= {"movieapp.baseUrl=http://localhost:8090"})
public class MoviesRestClientServerFaultTest {
    @Autowired
    private MoviesRestClient moviesRestClient;

    @Test
    void getAllMoviesWithServerError() {
        //given
        stubFor(get(anyUrl()).willReturn(serverError()));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
    }

    @Test
    void getAllMoviesWithServerError503() {
        //given
        String responseMessage = "Service Unavailable";
        stubFor(get(anyUrl())
            .willReturn(serverError()
                .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                .withBody(responseMessage)));

        //when
        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
        assertEquals(responseMessage, movieErrorResponse.getMessage());
    }

    @Test
    void getAllMoviesWithFaultResponse() {
        //given
        stubFor(get(anyUrl())
            .willReturn(aResponse()
                .withFault(Fault.EMPTY_RESPONSE)));

        //when
        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
        String errorMessage = "org.springframework.web.reactive.function.client.WebClientRequestException: Connection prematurely closed BEFORE response; nested exception is reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        assertEquals(errorMessage, movieErrorResponse.getMessage());
    }


    @Test
    void getAllMoviesWithRandomDataThenCLosed() {
        //given
        stubFor(get(anyUrl())
            .willReturn(aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
    }

    @Test
    void getAllMoviesWithFixedDelay() {
        //given
        stubFor(get(anyUrl())
            .willReturn(ok().withFixedDelay(10000)));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
    }
}
