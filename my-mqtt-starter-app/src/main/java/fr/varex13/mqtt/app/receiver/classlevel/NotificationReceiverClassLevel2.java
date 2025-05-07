package fr.varex13.mqtt.app.receiver.classlevel;

import org.springframework.stereotype.Component;

import fr.varex13.mqtt.app.Notification;
import fr.varex13.mqtt.receiver.MqttHandler;
import fr.varex13.mqtt.receiver.MqttListener;
import fr.varex13.mqtt.receiver.MqttListeners;

@Component
@MqttListeners({@MqttListener(topic = "alerts/notificationsClassLevel2_1", payloadType = Notification.class),@MqttListener(topic = "alerts/notificationsClassLevel2_2", payloadType = Notification.class)})
public class NotificationReceiverClassLevel2 {

    @MqttHandler
    public void handleNotification(Notification notification) {
        System.out.println("NotificationReceiverClassLevel2 --> Notification : " + notification.getMessage());
    }
}
