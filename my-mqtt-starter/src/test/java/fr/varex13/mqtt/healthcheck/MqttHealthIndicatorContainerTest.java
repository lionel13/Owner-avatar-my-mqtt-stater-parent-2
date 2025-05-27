package fr.varex13.mqtt.healthcheck;


import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.is;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
class MqttHealthIndicatorContainerTest {

    @Container
    private static final GenericContainer<?> mosquitto =
            new GenericContainer<>("eclipse-mosquitto:2.0")
                    .withExposedPorts(1888, 9001);

    @Autowired
    private MqttAsyncClient mqttClient;

    @BeforeEach
    void setUp() throws MqttException {
        String brokerUrl = String.format("tcp://%s:%d",
                mosquitto.getHost(), mosquitto.getMappedPort(1888));

//        mqttClient = new MqttAsyncClient(brokerUrl, "test-client-" + UUID.randomUUID());
//        MqttConnectionOptions options = new MqttConnectionOptions();
//        options.setCleanStart(true);
//        mqttClient.connect(options).waitForCompletion();
    }

    @AfterEach
    void tearDown() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect().waitForCompletion();
        }
        mqttClient.close();
    }

    @Test
    void should_report_up_when_mqtt_broker_is_available() throws Exception {
        MqttHealthIndicator indicator = new MqttHealthIndicator(mqttClient);
        Health.Builder builder = new Health.Builder();

        indicator.doHealthCheck(builder);
        Health health = builder.build();

        assertThat(health.getStatus().getCode(), is("UP"));
//        assertThat(health.getDetails(), Matchers.containsString("serverURI"));
//        assertThat(health.getDetails(), CoreMatchers.containsString("clientId", "serverURI"));
    }
}