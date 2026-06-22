package com.example.mesweb.global.mqtt;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqtttService {
	private final MqttConfig.myGateWay myGateWay;
	
	
	public void publish() {
		String msg = "hello mqtt";
		myGateWay.sendToMqtt(msg);
	}

}
