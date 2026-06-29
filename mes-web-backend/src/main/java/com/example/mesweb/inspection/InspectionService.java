package com.example.mesweb.inspection;

import org.springframework.stereotype.Service;

import com.example.mesweb.inspection.dto.InspectionMessage;
import com.example.mesweb.monitoring.MonitoringService;
import com.example.mesweb.monitoring.MonitoringSseService;
import com.example.mesweb.monitoring.dto.MonitoringSummary;
import com.example.mesweb.process.ProcessResult;
import com.example.mesweb.process.ProcessResultRepository;
import com.example.mesweb.schedule.Schedule;
import com.example.mesweb.schedule.ScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final ScheduleRepository scheduleRepository;
    private final ProcessResultRepository processResultRepository;
    private final MonitoringService monitoringService;
    private final MonitoringSseService monitoringSseService;
    

    public void process(String topic,InspectionMessage message) {
    	   validateTopic(topic, message);

           monitoringService.saveInspectionResult(message);

           MonitoringSummary summary = monitoringService.summary(message.scheduleId());

           monitoringSseService.sendMonitoringSummary(
                   message.scheduleId(),
                   summary
           );

    }
    
    private void validateTopic(String topic, InspectionMessage message) {
    	String expectedTopic = "mes/%s/%s/inspection"
    			.formatted(message.plantCode(), message.facilityId());
    	
    	if(!expectedTopic.equals(topic)) {
    		throw new IllegalArgumentException( "MQTT topic mismatch. topic=" + topic + ", expected=" + expectedTopic);
    	}
    }
}
