package fr.varex13.mqtt.app.temperature;

import org.springframework.stereotype.Service;

@Service
public class TemperatureService {
    public void traiterTemperature(Temperature temperature) {
        System.out.println("NotificationReceiverClassLevel1/handleNotification1 --> Notification : " + temperature);

    }
}
