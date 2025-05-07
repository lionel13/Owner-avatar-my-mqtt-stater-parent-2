package fr.varex13.mqtt.healthcheck;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class MqttHealthIndicator implements HealthIndicator {

    private final MqttAsyncClient mqttClient;

    public MqttHealthIndicator(MqttAsyncClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @Override
    public Health health() {
        if (mqttClient.isConnected()) {
            return Health.up()
                    .withDetail("clientId", mqttClient.getClientId())
                    .withDetail("serverURI", mqttClient.getServerURI())
                    .build();
        } else {
            return Health.down()
                    .withDetail("clientId", mqttClient.getClientId())
                    .withDetail("serverURI", mqttClient.getServerURI())
                    .withDetail("error", "Disconnected")
                    .build();
        }
    }
}
