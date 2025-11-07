package com.couponpop.couponservice.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.couponpop.couponservice.common.constants.RabbitMqConstants.*;

@EnableRabbit
@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Bean
    TopicExchange couponExchange() {
        return new TopicExchange(COUPON_EXCHANGE);
    }

    @Bean
    TopicExchange couponDLXExchange() {
        return new TopicExchange(COUPON_DLX_EXCHANGE);
    }

    // ---- ISSUED 이벤트용 ----
    @Bean
    public Queue issuedCouponQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", COUPON_DLX_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", COUPON_ISSUED_DLQ_ROUTING_KEY);
        return new Queue(COUPON_ISSUED_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Queue issuedCouponDLQQueue() {
        return new Queue(COUPON_ISSUED_DLQ_QUEUE, true);
    }

    @Bean
    public Binding issuedCouponBinding(TopicExchange couponExchange, Queue issuedCouponQueue) {
        return BindingBuilder
                .bind(issuedCouponQueue)
                .to(couponExchange)
                .with(COUPON_ISSUED_ROUTING_KEY);
    }

    @Bean
    public Binding issuedCouponDLQBinding(TopicExchange couponDLXExchange, Queue issuedCouponDLQQueue) {
        return BindingBuilder
                .bind(issuedCouponDLQQueue)
                .to(couponDLXExchange)
                .with(COUPON_ISSUED_DLQ_ROUTING_KEY);
    }

    // ---- USED 이벤트용 ----
    @Bean
    public Queue usedCouponQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", COUPON_DLX_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", COUPON_USED_DLQ_ROUTING_KEY);
        return new Queue(COUPON_USED_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Queue usedCouponDLQQueue() {
        return new Queue(COUPON_USED_DLQ_QUEUE, true);
    }

    @Bean
    public Binding usedCouponBinding(TopicExchange couponExchange, Queue usedCouponQueue) {
        return BindingBuilder
                .bind(usedCouponQueue)
                .to(couponExchange)
                .with(COUPON_USED_ROUTING_KEY);
    }

    @Bean
    public Binding usedCouponDLQBinding(Queue usedCouponDLQQueue, TopicExchange couponDLXExchange) {
        return BindingBuilder
                .bind(usedCouponDLQQueue)
                .to(couponDLXExchange)
                .with(COUPON_USED_DLQ_ROUTING_KEY);
    }

    // **** Common Setting ****
    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
