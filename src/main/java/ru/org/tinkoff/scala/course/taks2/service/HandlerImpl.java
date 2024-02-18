package ru.org.tinkoff.scala.course.taks2.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class HandlerImpl implements Handler {

    private final Client client;
    private final ScheduledExecutorService executorService;

    public HandlerImpl(Client client, ScheduledExecutorService executor) {
        this.client = client;
        this.executorService = executor;
    }

    @Override
    public Duration timeout() {
        return Duration.ofSeconds(10);
    }

    @Override
    public void performOperation() {
        Event event = client.readData();
        List<Address> recipients = event.recipients();
        Payload payload = event.payload();

        Callable<List<Address>> callable = () -> {
            List<Address> rejected = new ArrayList<>();
            recipients.forEach(address -> {
                Result result = client.sendData(address, payload);
                if (result == Result.REJECTED) {
                    rejected.add(address);
                }
            });
            return rejected;
        };

        Future<List<Address>> submit = executorService.submit(callable);
        List<Address> rejected;
        try {
            rejected = submit.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (!rejected.isEmpty()) {
            executorService.schedule(
                    () -> rejected.forEach(r -> client.sendData(r, payload)),
                    timeout().getSeconds(),
                    TimeUnit.SECONDS
            );
        }
    }
}
