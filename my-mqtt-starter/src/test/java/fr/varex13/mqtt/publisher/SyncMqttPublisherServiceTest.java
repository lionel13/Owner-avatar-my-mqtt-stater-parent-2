package fr.varex13.mqtt.publisher;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

class SyncMqttPublisherServiceTest {

    private MqttAsyncClient mockClient;
    private ObjectMapper objectMapper;
    private SyncMqttPublisherService publisher;

    @BeforeEach
    void setup() {
        mockClient = mock(MqttAsyncClient.class);
        objectMapper = new ObjectMapper();
        publisher = new SyncMqttPublisherService(mockClient, 1, objectMapper);
    }

    @Test
    void shouldThrowExceptionWhenTopicIsNull() {
        when(mockClient.isConnected()).thenReturn(true);
        InvalidMqttPublishRequestException exception = assertThrows(
                InvalidMqttPublishRequestException.class,
                () -> publisher.publish(null, new DummyPayload("test"))
        );
        assertEquals("MQTT topic must not be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTopicIsBlank() {
        when(mockClient.isConnected()).thenReturn(true);
        InvalidMqttPublishRequestException exception = assertThrows(
                InvalidMqttPublishRequestException.class,
                () -> publisher.publish("   ", new DummyPayload("test"))
        );
        assertEquals("MQTT topic must not be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPayloadIsNull() {
        when(mockClient.isConnected()).thenReturn(true);
        InvalidMqttPublishRequestException exception = assertThrows(
                InvalidMqttPublishRequestException.class,
                () -> publisher.publish("test/topic", null)
        );
        assertEquals("MQTT payload must not be null", exception.getMessage());
    }

    @Test
    void shouldNotPublishWhenClientIsNotConnected() throws Exception {
        when(mockClient.isConnected()).thenReturn(false);
        publisher.publish("test/topic", new DummyPayload("no-connect"));
        verify(mockClient, never()).publish(any(), any(), any(), any());
    }

    @Test
    void shouldPublishSuccessfullyWithCallbackAndWait() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);
        IMqttToken mockToken = mock(IMqttToken.class);
        when(mockClient.publish(anyString(), any(MqttMessage.class), any(), any(MqttActionListener.class)))
                .thenReturn(mockToken);

        DummyPayload payload = new DummyPayload("ok");
        String topic = "test/topic";
        publisher.publish(topic, payload);

        ArgumentCaptor<MqttMessage> messageCaptor = ArgumentCaptor.forClass(MqttMessage.class);
        ArgumentCaptor<MqttActionListener> listenerCaptor = ArgumentCaptor.forClass(MqttActionListener.class);

        verify(mockClient).publish(eq(topic), messageCaptor.capture(), isNull(), listenerCaptor.capture());
        verify(mockToken).waitForCompletion();

        MqttMessage message = messageCaptor.getValue();
        String expected = objectMapper.writeValueAsString(payload);
        assertEquals(expected, new String(message.getPayload()));
        assertEquals(1, message.getQos());

        assertNotNull(listenerCaptor.getValue());
    }

    @Test
    void shouldThrowExceptionOnSerializationError() {
        when(mockClient.isConnected()).thenReturn(true);

        Object badPayload = new Object() {
            public Object self = this; // cause circular reference
        };

        MqttPublishException ex = assertThrows(MqttPublishException.class, () ->
                publisher.publish("bad/topic", badPayload));

        assertTrue(ex.getMessage().contains("Failed to serialize"));
    }

    @Test
    void shouldThrowExceptionOnMqttFailure() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);
        when(mockClient.publish(anyString(), any(MqttMessage.class), any(), any()))
                .thenThrow(new MqttException(42));

        MqttPublishException ex = assertThrows(MqttPublishException.class, () ->
                publisher.publish("mqtt/failure", new DummyPayload("fail")));

        assertTrue(ex.getMessage().contains("Failed to publish MQTT message"));
    }

    static class DummyPayload {
        public String message;

        public DummyPayload() {
        }

        public DummyPayload(String message) {
            this.message = message;
        }
    }
}
