package fr.varex13.mqtt.publisher;

import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.springframework.core.log.LogAccessor;

public class MyMqttActionListener implements MqttActionListener {

    private final LogAccessor logger = new LogAccessor(LogFactory.getLog(getClass()));

    private final String topic;

    public static MyMqttActionListener myMqttActionListenerForTopic(String topic) {
        return new MyMqttActionListener(topic);
    }

    private MyMqttActionListener(String topic) {
        this.topic = topic;
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        logger.debug(() -> "Published to topic [" + topic + "] successfully.");
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        logger.error(exception, () -> "Failed to publish message to topic [" + topic + "]");
    }
}
