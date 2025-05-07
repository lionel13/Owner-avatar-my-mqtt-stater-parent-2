package fr.varex13.mqtt.app.receiver.methodlevel;

import org.springframework.stereotype.Component;

import fr.varex13.mqtt.app.Notification;
import fr.varex13.mqtt.receiver.MqttListener;

@Component
public class NotificationReceiverMethodLevel1 {

    @MqttListener(topic = "alerts/notificationsMethodLevel1", payloadType = Notification.class)
    public void handleNotification(Notification notification) {
        System.out.println("NotificationReceiverMethodLevel1 --> Notification : " + notification.getMessage());
    }
}
