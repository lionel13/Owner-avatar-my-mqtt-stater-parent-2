package fr.varex13.mqtt.publisher;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.core.log.LogAccessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractMqttPublisherService implements PublisherStrategy {

    protected final LogAccessor logger = new LogAccessor(LogFactory.getLog(getClass()));

    protected final MqttAsyncClient client;
    protected final int qos;
    protected final ObjectMapper objectMapper;

    protected AbstractMqttPublisherService(final MqttAsyncClient client,
                                           final int qos,
                                           final ObjectMapper objectMapper) {
        this.client = client;
        this.qos = qos;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void publish(final String topic, final T payload) {
        if (!client.isConnected()) {
            logger.warn("MQTT client not connected. Message not published.");
            return;
        }

        validateInput(topic, payload);

        try {
            final MqttMessage message = createMqttMessage(payload, topic);
            doPublish(topic, message);
        } catch (JsonProcessingException e) {
            throw new MqttPublishException("Failed to serialize payload for topic [" + topic + "]", e);
        } catch (MqttException e) {
            throw new MqttPublishException("Failed to publish MQTT message", e);
        }
    }
    protected <T> void validateInput(String topic, T payload) {
        if (topic == null || topic.isBlank()) {
            throw new InvalidMqttPublishRequestException("MQTT topic must not be null or blank");
        }
        if (payload == null) {
            throw new InvalidMqttPublishRequestException("MQTT payload must not be null");
        }
    }


    private <T> MqttMessage createMqttMessage(T payload, String topic) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(payload);
        logger.debug(() -> "Publishing to topic [" + topic + "]: " + json);
        MqttMessage message = new MqttMessage(json.getBytes(UTF_8));
        message.setQos(qos);
        return message;
    }

    protected abstract void doPublish(String topic, MqttMessage message) throws MqttException;
}
