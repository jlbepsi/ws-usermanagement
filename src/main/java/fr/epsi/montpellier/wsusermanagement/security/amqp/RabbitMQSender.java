package fr.epsi.montpellier.wsusermanagement.security.amqp;


import fr.epsi.montpellier.Ldap.UserLdap;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    @Value("${rabbitmq.topicName}")
    private String topicName;
    @Value("${rabbitmq.routingKey}")
    private String routingKey;


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

    // TODO Delete this method
    public void sendDebugMessage(String message) {
        rabbitTemplate.convertAndSend(topicName, routingKey, message);

    }

    private void sendMessage(String status, String login, String message) {
        rabbitTemplate.convertAndSend(topicName, routingKey,
                String.format("{'status':'%s', 'login': '%s', 'message':'%s'}", status, login, message)
        );
    }
}
