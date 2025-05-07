package fr.varex13.mqtt.app.receiver.methodlevel;

import org.springframework.stereotype.Component;

import fr.varex13.mqtt.app.Notification;
import fr.varex13.mqtt.receiver.MqttListener;
import fr.varex13.mqtt.receiver.MqttListeners;

@Component
public class NotificationReceiverMethodLevel2 {

    @MqttListeners({
            @MqttListener(topic = "alerts/notificationsMethodLevel2_1", payloadType = Notification.class),
            @MqttListener(topic = "alerts/notificationsMethodLevel2_2", payloadType = Notification.class)})
    public void handleNotification(Notification notification) {
        System.out.println("NotificationReceiverMethodLevel2 --> Notification : " + notification.getMessage());
    }
}
