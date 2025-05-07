package fr.varex13.mqtt.app;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.varex13.mqtt.app.temperature.Temperature;
import fr.varex13.mqtt.publisher.PublisherStrategy;

@RestController
public class SendController {

    private final PublisherStrategy publisher;

    public SendController(PublisherStrategy publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/send")
    public void send(@RequestBody Notification notif) {
        publisher.publish("alerts/notificationsClassLevel1", notif);
        publisher.publish("alerts/notificationsClassLevel2_1", notif);
        publisher.publish("alerts/notificationsClassLevel2_2", notif);
        publisher.publish("alerts/notificationsClassLevel3_1", notif);
        publisher.publish("alerts/notificationsClassLevel3_2", notif);
        publisher.publish("alerts/notificationsMethodLevel1", notif);
        publisher.publish("alerts/notificationsMethodLevel2_1", notif);
        publisher.publish("alerts/notificationsMethodLevel2_2", notif);
        publisher.publish("alerts/notificationsMethodLevel3_1", notif);
        publisher.publish("alerts/notificationsMethodLevel3_2", notif);
        publisher.publish("capteurs/temperature", new Temperature(24, LocalDateTime.now()));
    }
}

