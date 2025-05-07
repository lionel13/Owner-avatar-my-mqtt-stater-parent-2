package fr.varex13.mqtt.publisher;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Objects;

import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.core.log.LogAccessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AsyncMqttPublisherService implements PublisherStrategy {

    private final LogAccessor logger = new LogAccessor(LogFactory.getLog(getClass()));
    private final MqttAsyncClient client;
    private final int qos;
    private final ObjectMapper objectMapper;

    public AsyncMqttPublisherService(MqttAsyncClient client, int qos, ObjectMapper objectMapper) {
        this.client = client;
        this.qos = qos;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void publish(String topic, T payload) {

        if (!client.isConnected()) {
            logger.warn("MQTT client not connected. Message not published.");
            return;
        }

        Objects.requireNonNull(topic);
        Objects.requireNonNull(payload);

        try {
            String json = objectMapper.writeValueAsString(payload);
            MqttMessage message = new MqttMessage(json.getBytes(UTF_8));
            message.setQos(qos);

            logger.debug(() -> "Publishing asynchronously to topic [" + topic + "]: " + json);

            client.publish(topic, message, null, new MqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    logger.debug(() -> "Published to topic [" + topic + "] successfully.");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    logger.error(exception, () -> "Failed to publish message to topic [" + topic + "]");
                }
            });
        } catch (JsonProcessingException | MqttException e) {
            throw new MqttPublishException("Failed to initiate asynchronous publish", e);
        }
    }
}

