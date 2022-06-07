package fr.epsi.montpellier.wsusermanagement.security.amqp;


import fr.epsi.montpellier.Ldap.UserLdap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    @Value("${rabbitmq.topicName}")
    private String topicName;
    @Value("${rabbitmq.routingKey}")
    private String routingKey;


    // Logger
    private static final Logger logger = LogManager.getLogger("MQSender");
    // RabbitMQ
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAddMessage(UserLdap user) {
        sendMessage("1",  user.getLogin(), "Add user");
    }

    public void sendDeleteMessage(String login) {
        sendMessage("-1", login, "Delete user");
    }

    private void sendMessage(String status, String login, String message) {
        String amqpMessage = String.format("{\"status\":\"%s\", \"login\": \"%s\", \"message\":\"%s\"}", status, login, message);
        try {
            rabbitTemplate.convertAndSend(topicName, routingKey, amqpMessage);
        } catch (AmqpException amqpException) {
            logger.error(String.format("RabbitSend('%s') - AMQP Error", amqpMessage), amqpException);
        } catch (Exception exception) {
            logger.error(String.format("RabbitSend('%s') - General Error", amqpMessage), exception);
        }
    }
}
