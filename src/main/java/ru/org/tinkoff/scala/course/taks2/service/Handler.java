package ru.org.tinkoff.scala.course.taks2.service;

import java.time.Duration;

public interface Handler {

    Duration timeout();

    void performOperation();
}