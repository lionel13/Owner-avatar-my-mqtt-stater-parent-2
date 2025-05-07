package fr.varex13.mqtt;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.varex13.mqtt.healthcheck.MqttHealthIndicator;
import fr.varex13.mqtt.publisher.AsyncMqttPublisherService;
import fr.varex13.mqtt.publisher.PublisherStrategy;
import fr.varex13.mqtt.publisher.SyncMqttPublisherService;
import fr.varex13.mqtt.receiver.MqttMessageDispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MqttAsyncClient mqttClient(MqttProperties props) throws MqttException {
        MqttAsyncClient client = new MqttAsyncClient(props.getHost(), props.getClientId());

        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setUserName(props.getUsername());
        options.setPassword(props.getPassword().getBytes());
        options.setCleanStart(true); // important pour MQTT v5

        client.connect(options).waitForCompletion();

        return client;
    }

    @Bean
    @ConditionalOnProperty(name = "mqtt.publisher.async", havingValue = "true")
    public PublisherStrategy asyncPublisher(MqttAsyncClient client, ObjectMapper mapper) {
        return new AsyncMqttPublisherService(client, 1, mapper);
    }

    @Bean
    @ConditionalOnProperty(name = "mqtt.publisher.async", havingValue = "false", matchIfMissing = true)
    public PublisherStrategy syncPublisher(MqttAsyncClient client, ObjectMapper mapper) {
        return new SyncMqttPublisherService(client, 1, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttMessageDispatcher mqttMessageDispatcher(MqttAsyncClient client, ObjectMapper objectMapper) {
        return new MqttMessageDispatcher(client, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public HealthIndicator mqttHealthIndicator(MqttAsyncClient mqttAsyncClient) {
        return new MqttHealthIndicator(mqttAsyncClient);
    }
}
