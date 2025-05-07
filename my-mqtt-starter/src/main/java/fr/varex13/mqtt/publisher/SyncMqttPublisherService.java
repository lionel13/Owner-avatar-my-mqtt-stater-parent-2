package fr.varex13.mqtt.publisher;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Objects;

import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.core.log.LogAccessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SyncMqttPublisherService implements PublisherStrategy {

    private final LogAccessor logger = new LogAccessor(LogFactory.getLog(getClass()));

    private final MqttAsyncClient client;
    private final int qos;
    private final ObjectMapper objectMapper;

    public SyncMqttPublisherService(final MqttAsyncClient client, final int qos, final ObjectMapper objectMapper) {
        this.client = client;
        this.qos = qos;
        this.objectMapper = objectMapper;
    }

    public <T> void publish(final String topic, final T payload) {

        if (!client.isConnected()) {
            logger.warn("MQTT client not connected. Message not published.");
            return;
        }

        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        try {

            final String json = objectMapper.writeValueAsString(payload);
            final MqttMessage message = new MqttMessage(json.getBytes(UTF_8));
            message.setQos(qos);

            logger.debug(() -> "Publishing to topic [" + topic + "]: " + json);
            client.publish(topic, message).waitForCompletion();
            logger.debug(() -> "Successfully published message to topic [" + topic + "]");
        } catch (JsonProcessingException e) {
            throw new MqttPublishException("Failed to serialize payload for topic [" + topic + "]", e);
        } catch (MqttException e) {
            throw new MqttPublishException("Failed to publish MQTT message", e);
        }
    }
}