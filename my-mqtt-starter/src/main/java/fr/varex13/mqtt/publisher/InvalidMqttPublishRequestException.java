package fr.varex13.mqtt.publisher;

public class InvalidMqttPublishRequestException extends RuntimeException {
    public InvalidMqttPublishRequestException(String message) {
        super(message);
    }
}
