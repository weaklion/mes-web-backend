package com.example.mesweb.global.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import com.example.mesweb.inspection.InspectionService;
import com.example.mesweb.inspection.dto.InspectionMessage;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;



@Configuration
public class MqttConfig {
	
	@Value("${mqtt.url}")
	private String mqttUrl;
	
	@Value("${mqtt.client-id}")
	private String clientId;
	
	@Value("${mqtt.topic}")
	private String topic;

	//클라이언트 팩토리
	@Bean
	public MqttPahoClientFactory mqttClientFactory() {		
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();
		options.setServerURIs(new String[]{mqttUrl});
		factory.setConnectionOptions(options);
		return factory;
	}
	
	//채널 구성
	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutBound() {
	MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, mqttClientFactory());
	messageHandler.setAsync(true);
	messageHandler.setDefaultTopic(topic);
	return messageHandler;
	}
	
	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}
	
	//메시지 전송 채널
	@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
	public interface myGateWay{
		void sendToMqtt(String data);
	}
	
	@Bean
	public MessageChannel mqttInputChannel() {
		// Point to Point Channel 중 가장 기본
				// messageHandler에게 Message를 전송
				// DirectChannel을 구독하는 핸들러 하나에게만 브로드 캐스트

				// PublishSubscribeChannel은 Publish/Subscribe Channel로
				// 해당 채널을 구독한 모든 핸들러에게 브로드 캐스트
		return new DirectChannel();
		
	}
	
	@Bean
	public MessageProducer inbound() {
		// 메시지 수신을 위한 채널을 구성
		// 생성자를 통해 topic을 여러개 추가할 수 있음.
		// addTopic() 메서드도 존재함.
		MqttPahoMessageDrivenChannelAdapter adapter = 
				new MqttPahoMessageDrivenChannelAdapter(mqttUrl, clientId, topic);
	
		adapter.setCompletionTimeout(5000); 
		adapter.setConverter(new DefaultPahoMessageConverter()); // MQTT 메시지를 Spring Integration 메시지로 변환(json 처리하기 위해 필요)		
		adapter.setQos(1); 
		adapter.setOutputChannel(mqttInputChannel()); // Spring Integration 채널 설정
		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel") 
	public MessageHandler messageHandler(ObjectMapper objectMapper,InspectionService inspectionService) {
		//broker -> inbound -> mqttInputChannel -> messageHandler
		return message -> {
			try {
				String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
				
				String payload = message.getPayload().toString();
				
				// json 문자열을 객체로 변환 
				InspectionMessage inspectionMessage = objectMapper.readValue(payload, InspectionMessage.class);
				
				inspectionService.process(topic, inspectionMessage);
			} catch (Exception e) {
	            throw new IllegalArgumentException(
	                    "MQTT 메시지 변환 실패",
	                    e
	            );
	        }
		};

	}
}
