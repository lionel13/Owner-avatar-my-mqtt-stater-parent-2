package fr.varex13.mqtt.publisher;

import static fr.varex13.mqtt.publisher.MyMqttActionListener.myMqttActionListenerForTopic;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SyncMqttPublisherService<T> extends AbstractMqttPublisherService<T> {

    public SyncMqttPublisherService(MqttAsyncClient client, int qos, ObjectMapper objectMapper) {
        super(client, qos, objectMapper);
    }

    @Override
    protected void doPublish(String topic, MqttMessage message) throws MqttException {
        client.publish(topic, message, null, myMqttActionListenerForTopic(topic)).waitForCompletion();
    }
}
