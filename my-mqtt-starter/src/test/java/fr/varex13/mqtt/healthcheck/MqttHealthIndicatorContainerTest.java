package fr.varex13.mqtt.healthcheck;


import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.is;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import fr.varex13.mqtt.MyTestConfiguration;

//@ContextConfiguration
@SpringBootTest(classes = MyTestConfiguration.class)
//@Import(MyTestConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
class MqttHealthIndicatorContainerTest {
	@Container
	static GenericContainer<?> mosquittoContainer = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0"))
			.withExposedPorts(1888)
			.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Mosquitto")));

	@Autowired
	private MqttAsyncClient mqttClient;

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("mqtt.host", mosquittoContainer::getHost);
//		registry.add("spring.datasource.username", mosquittoContainer::getP);
	}

	@BeforeAll
	static void beforeAll() {
		mosquittoContainer.start();
	}

	@BeforeEach
	void setUp() throws MqttException {
	}

	@AfterAll
	static void afterAll() {
		mosquittoContainer.stop();
	}


//    @AfterEach
//    void tearDown() throws MqttException {
//        if (mqttClient.isConnected()) {
//            mqttClient.disconnect().waitForCompletion();
//        }
//        mqttClient.close();
//    }

	@Test
	void should_report_up_when_mqtt_broker_is_available() throws Exception {

		String brokerUrl = String.format("tcp://%s:%d",
				mosquittoContainer.getHost(), mosquittoContainer.getMappedPort(1888));

		mqttClient = new MqttAsyncClient(brokerUrl, "test-client-" + UUID.randomUUID());
		MqttConnectionOptions options = new MqttConnectionOptions();
		options.setCleanStart(true);
		mqttClient.connect(options).waitForCompletion();
		MqttHealthIndicator indicator = new MqttHealthIndicator(mqttClient);
		Health.Builder builder = new Health.Builder();

		indicator.doHealthCheck(builder);
		Health health = builder.build();

		assertThat(health.getStatus().getCode(), is("UP"));
//        assertThat(health.getDetails(), Matchers.containsString("serverURI"));
//        assertThat(health.getDetails(), CoreMatchers.containsString("clientId", "serverURI"));
	}
}