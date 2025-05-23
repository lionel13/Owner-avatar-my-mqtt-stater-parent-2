package fr.varex13.mqtt.app.temperature;

import org.springframework.stereotype.Component;

import fr.varex13.mqtt.receiver.MqttHandler;
import fr.varex13.mqtt.receiver.MqttListener;

@Component
@MqttListener(topic = "grafana/test", payloadType = Integer.class)
public class TemperatureReceiverForGrafana {

    private final TemperatureService temperatureService;

    public TemperatureReceiverForGrafana(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @MqttHandler
    public void handleNotification1(final Integer temperature) {
        System.out.println(" GRAFANA -> " + temperature);
    }

}
