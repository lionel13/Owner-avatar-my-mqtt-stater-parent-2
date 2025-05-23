package fr.varex13.mqtt.publisher;

public interface PublisherStrategy<T> {
    void publish(String topic, T payload);
    void publish(String topic, int qos, T payload);
}
