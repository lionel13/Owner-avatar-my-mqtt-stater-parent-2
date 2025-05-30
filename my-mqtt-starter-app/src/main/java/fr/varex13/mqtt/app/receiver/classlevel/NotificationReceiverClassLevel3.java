package fr.varex13.mqtt.app.receiver.classlevel;

import org.springframework.stereotype.Component;

import fr.varex13.mqtt.app.Notification;
import fr.varex13.mqtt.receiver.MqttHandler;
import fr.varex13.mqtt.receiver.MqttListener;

@Component
@MqttListener(topic = "alerts/notificationsClassLevel3_1", payloadType = Notification.class)
@MqttListener(topic = "alerts/notificationsClassLevel3_2", payloadType = Notification.class)
public class NotificationReceiverClassLevel3 {

    @MqttHandler
    public void handleNotification1(Notification notification) {
        System.out.println("NotificationReceiverClassLevel3/handleNotification1 --> Notification : " + notification.getMessage());
    }

}
