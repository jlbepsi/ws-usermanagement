package fr.epsi.montpellier.wsusermanagement.security.amqp;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;


// https://www.cloudamqp.com/blog/2015-05-18-part1-rabbitmq-for-beginners-what-is-rabbitmq.html

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.queueName}")
    private String queue;
    @Value("${rabbitmq.topicName}")
    private String topicName;
    @Value("${rabbitmq.routingkey}")
    private String routingKey;

    @Bean
    public TopicExchange getExchange() {
        return new TopicExchange(topicName);
    }

    @Bean
    public Queue getQueue() {
        return new Queue(queue);
    }

    @Bean
    public Binding declareBinding() {
        return BindingBuilder.bind(getQueue()).to(getExchange()).with(routingKey);
    }
}
