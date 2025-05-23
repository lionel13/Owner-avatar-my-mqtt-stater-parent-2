package fr.varex13.mqtt.publisher;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.core.log.LogAccessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractMqttPublisherService<T> implements PublisherStrategy<T> {

    protected final LogAccessor logger = new LogAccessor(LogFactory.getLog(getClass()));

    protected final MqttAsyncClient client;
    private final int qos;
    protected final ObjectMapper objectMapper;

    protected AbstractMqttPublisherService(final MqttAsyncClient client,
                                           final int qos,
                                           final ObjectMapper objectMapper) {
        this.client = client;
        this.qos = qos;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(final String topic, final T payload) {
        publish(topic, qos, payload);
    }

    @Override
    public void publish(final String topic, int qos, final T payload) {

        validateInput(topic, payload);

        if (!client.isConnected()) {
            logger.warn("MQTT client not connected. Message not published.");
            return;
        }

        try {
            final MqttMessage message = createMqttMessage(payload, qos, topic);
            doPublish(topic, message);
        } catch (final JsonProcessingException e) {
            throw new MqttPublishException("Failed to serialize payload for topic [" + topic + "]", e);
        } catch (final MqttException e) {
            throw new MqttPublishException("Failed to publish MQTT message", e);
        }

    }

    protected void validateInput(final String topic, final T payload) {
        if (isNull(topic) || topic.isBlank()) {
            throw new InvalidMqttPublishRequestException("MQTT topic must not be null or blank");
        }
        if (isNull(payload)) {
            throw new InvalidMqttPublishRequestException("MQTT payload must not be null");
        }
    }

    private MqttMessage createMqttMessage(T payload, int qos, String topic) throws JsonProcessingException {
        final String json = objectMapper.writeValueAsString(payload);
        logger.debug(() -> "Publishing to topic [" + topic + "]: " + json);
        final MqttMessage message = new MqttMessage(json.getBytes(UTF_8));
        message.setQos(qos);
        return message;
    }

    protected abstract void doPublish(String topic, MqttMessage message) throws MqttException;
}
