package fr.varex13.mqtt.healthcheck;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.Assert;

/**
 * Simple implementation of a {@link HealthIndicator} returning status information for
 * MQTT Broker.
 *
 * @author Lionel Audibert
 * @since 1.0.0
 */
public class MqttHealthIndicator extends AbstractHealthIndicator {

    private final MqttAsyncClient mqttClient;

    public MqttHealthIndicator(MqttAsyncClient mqttClient) {
        super("MQTT health check failed");
        Assert.notNull(mqttClient, "MqttAsyncClient must not be null");
        this.mqttClient = mqttClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            if (mqttClient.isConnected()) {
                builder.up()
                        .withDetail("clientId", mqttClient.getClientId())
                        .withDetail("serverURI", mqttClient.getServerURI())
                        .withDetail("lastReconnectTime", mqttClient.getCurrentServerURI())
                        .build();
            } else {
                builder.down()
                        .withDetail("clientId", mqttClient.getClientId())
                        .withDetail("serverURI", mqttClient.getServerURI())
                        .withDetail("error", "Disconnected")
                        .build();
            }
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
