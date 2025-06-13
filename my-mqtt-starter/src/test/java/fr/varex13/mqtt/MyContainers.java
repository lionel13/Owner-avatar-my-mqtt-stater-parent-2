package fr.varex13.mqtt;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class MyContainers {

	@Container
	GenericContainer<?> mosquittoContainer = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0"));
}
