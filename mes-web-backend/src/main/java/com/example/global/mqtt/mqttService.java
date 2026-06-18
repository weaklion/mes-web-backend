package com.example.global.mqtt;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class mqttService {
	private final mqttConfig.myGateWay myGateWay;
	
	public void process() {
		String msg = "hello mqtt";
		myGateWay.sendToMqtt(msg);
	}

}
