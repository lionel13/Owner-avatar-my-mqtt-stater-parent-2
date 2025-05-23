package fr.varex13.mqtt.app;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.varex13.mqtt.app.temperature.Temperature;
import fr.varex13.mqtt.publisher.PublisherStrategy;

@RestController
public class SendControllerForTemperature {

    private final PublisherStrategy<Temperature> publisher;

    public SendControllerForTemperature(PublisherStrategy<Temperature> publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/sendTemperature")
    public void send() {
        publisher.publish("capteurs/temperature", new Temperature(24, LocalDateTime.now()));
    }
}

