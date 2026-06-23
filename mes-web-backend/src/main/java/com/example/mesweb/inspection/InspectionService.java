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
    
    @Transactional
    public void process(String topic,InspectionMessage message) {
    	Schedule schedule = scheduleRepository.findById(message.scheduleId()).orElseThrow(() -> new IllegalArgumentException("공정계획을 찾을 수 없습니다."));
    	boolean success = "OK".equalsIgnoreCase(message.result());
    	
    	ProcessResult processResult = new ProcessResult(
    			  schedule.getSchIdx(),              // schIdx
    			    message.eventId(),                 // prcCd
    			    message.inspectedAt().toLocalDate(), // prcDate
    			    schedule.getLoadTime(),            // prcLoadTime
    			    message.facilityId(),              // prcFacilityId
    			    success                            // prcResult
    			   );
    	    	
    	processResultRepository.save(processResult);
    	
    	MonitoringSummary summary = monitoringService.summary(message.scheduleId());
    	
    	monitoringSseService.sendInspectionResult(
    		    message.scheduleId(),
    		    summary
    		);

    }
}
