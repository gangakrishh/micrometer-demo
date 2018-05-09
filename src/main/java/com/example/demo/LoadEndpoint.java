package com.example.demo;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.stream.Stream;

public class LoadEndpoint {
    public static void main(String[] args) {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

        WebClient client = WebClient.create("http://localhost:8080");

        RandomEngine r = new MersenneTwister64(0);
        Normal requestBurst = new Normal(250, 50, r);

        Flux.fromStream(Stream.iterate(0, n -> (int) requestBurst.nextDouble()))
                .delayElements(Duration.ofSeconds(1))
                .parallel()
                .runOn(Schedulers.parallel())
                .doOnEach(n -> {
                    for (int i = 0; i < n.get(); i++) {
                        client.get().uri("/teams").exchange().block()
                                .bodyToMono(String.class)
                                .block();
                    }
                })
                .subscribe();

        Flux.never().blockLast();
    }
}
