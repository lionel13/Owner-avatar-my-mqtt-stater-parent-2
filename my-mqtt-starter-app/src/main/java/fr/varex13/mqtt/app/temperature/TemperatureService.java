package fr.varex13.mqtt.app.temperature;

import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import fr.varex13.mqtt.publisher.PublisherStrategy;

@Service
public class TemperatureService {

    private final PublisherStrategy<Integer> publisher;
    private Random random = new Random();

    public TemperatureService(PublisherStrategy<Integer> publisher) {
        this.publisher = publisher;
    }

    public void traiterTemperature(Temperature temperature) {
        System.out.println("NotificationReceiverClassLevel1/handleNotification1 --> Notification : " + temperature);
    }

    @Scheduled(fixedRate = 1000)
    public void zaza() {
        publisher.publish("grafana/test", random.nextInt(50));
    }
}
