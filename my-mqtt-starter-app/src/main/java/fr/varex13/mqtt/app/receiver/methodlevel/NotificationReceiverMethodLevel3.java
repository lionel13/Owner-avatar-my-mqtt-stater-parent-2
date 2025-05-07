package fr.varex13.mqtt.app.receiver.methodlevel;

import org.springframework.stereotype.Component;

import fr.varex13.mqtt.app.Notification;
import fr.varex13.mqtt.receiver.MqttListener;

@Component
public class NotificationReceiverMethodLevel3 {

    @MqttListener(topic = "alerts/notificationsMethodLevel3_1", payloadType = Notification.class)
    @MqttListener(topic = "alerts/notificationsMethodLevel3_2", payloadType = Notification.class)
    public void handleNotification(Notification notification) {
        System.out.println("NotificationReceiverMethodLevel3 --> Notification : " + notification.getMessage());
    }
}
