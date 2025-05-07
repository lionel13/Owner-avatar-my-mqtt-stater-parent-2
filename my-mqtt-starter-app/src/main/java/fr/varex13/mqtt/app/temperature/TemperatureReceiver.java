package fr.varex13.mqtt.app.temperature;

import org.springframework.stereotype.Component;

import fr.varex13.mqtt.app.Notification;
import fr.varex13.mqtt.receiver.MqttHandler;
import fr.varex13.mqtt.receiver.MqttListener;

@Component
@MqttListener(topic = "capteurs/temperature", payloadType = Temperature.class)
public class TemperatureReceiver {

    private final TemperatureService temperatureService;

    public TemperatureReceiver(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @MqttHandler
    public void handleNotification1(final Temperature temperature) {
        temperatureService.traiterTemperature(temperature);
    }

}
