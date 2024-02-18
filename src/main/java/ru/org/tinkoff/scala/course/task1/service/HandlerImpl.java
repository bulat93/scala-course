package ru.org.tinkoff.scala.course.task1.service;

import ru.org.tinkoff.scala.course.task1.service.ApplicationStatusResponse;
import ru.org.tinkoff.scala.course.task1.service.Client;
import ru.org.tinkoff.scala.course.task1.service.Handler;
import ru.org.tinkoff.scala.course.task1.service.Response;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class HandlerImpl implements Handler {

    private static final long DELAY_IN_SECONDS = 15;

    private final Client client;
    private final Executor executor;

    public HandlerImpl(Client client) {
        this.client = client;
        this.executor = CompletableFuture.delayedExecutor(DELAY_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {
        AtomicReference<Long> timeMillis = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger(0);

        CompletableFuture<Response> task1 = CompletableFuture.supplyAsync(
                () -> client.getApplicationStatus1(id), executor
        ).handle((response, throwable) -> {
            count.incrementAndGet();
            timeMillis.set(System.currentTimeMillis());
            return null;
        });

        CompletableFuture<Response> task2 = CompletableFuture.supplyAsync(
                () -> client.getApplicationStatus2(id), executor
        ).handle((response, throwable) -> {
            count.incrementAndGet();
            timeMillis.set(System.currentTimeMillis());
            return null;
        });

        Response response = Stream.of(task1, task2)
                .map(CompletableFuture::join)
                .findFirst()
                .get();

        if (response instanceof Response.Success successResponse) {
            return new ApplicationStatusResponse.Success(
                    successResponse.applicationId(),
                    successResponse.applicationStatus());
        } else {
            return new ApplicationStatusResponse.Failure(
                    Duration.ofMillis(timeMillis.get()),
                    count.get()
            );
        }
    }
}
