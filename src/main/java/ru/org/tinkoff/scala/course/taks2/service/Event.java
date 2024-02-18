package ru.org.tinkoff.scala.course.taks2.service;

import java.util.List;

public record Event(List<Address> recipients, Payload payload) {}