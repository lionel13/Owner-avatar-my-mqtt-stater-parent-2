package fr.varex13.mqtt.app.temperature;

import java.time.LocalDateTime;

public record Temperature(int temperature, LocalDateTime heure) {

}
