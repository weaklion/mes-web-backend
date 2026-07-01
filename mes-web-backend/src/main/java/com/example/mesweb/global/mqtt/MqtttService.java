package com.example.mesweb.global.mqtt;

import org.springframework.stereotype.Service;

import com.example.mesweb.inspection.dto.InspectionMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqtttService {
	private final MqttConfig.myGateWay myGateWay;
	private final ObjectMapper objectMapper;
	
	
	public void publishInspectionResult(InspectionMessage message) {
		String topic = "mes/%s/%s/inspection".formatted(message.plantCode(), message.facilityId());

		try {
			String payload = objectMapper.writeValueAsString(message);
			myGateWay.sendToMqtt(payload, topic);
			log.info("Published simulated inspection result. topic={}, payload={}", topic, payload);
		} catch (Exception e) {
			throw new IllegalArgumentException("MQTT inspection result publish failed", e);
		}
	}

}
