package com.example.mesweb.monitoring;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mesweb.inspection.dto.InspectionMessage;
import com.example.mesweb.inspection.dto.InspectionResultRequest;
import com.example.mesweb.monitoring.dto.ControlMessage;
import com.example.mesweb.monitoring.dto.MonitoringSummary;
import com.example.mesweb.process.ProcessResult;
import com.example.mesweb.process.ProcessResultRepository;
import com.example.mesweb.schedule.Schedule;
import com.example.mesweb.schedule.ScheduleRepository;
import com.example.mesweb.setting.SettingRepository;



@Service
public class MonitoringService {
    private final ScheduleRepository scheduleRepository;
    private final SettingRepository settingRepository;
    private final ProcessResultRepository processResultRepository;

    public MonitoringService(ScheduleRepository scheduleRepository, SettingRepository settingRepository,
                             ProcessResultRepository processResultRepository) {
        this.scheduleRepository = scheduleRepository;
        this.settingRepository = settingRepository;
        this.processResultRepository = processResultRepository;
    }

    @Transactional(readOnly = true)
    public MonitoringSummary summary(int schIdx) {
        Schedule schedule = scheduleRepository.findById(schIdx)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + schIdx));
        String plantName = settingRepository.findById(schedule.getPlantCode())
                .map(setting -> setting.getCodeName())
                .orElse(schedule.getPlantCode());
        String facilityName = settingRepository.findById(schedule.getSchFacilityId())
                .map(setting -> setting.getCodeName())
                .orElse(schedule.getSchFacilityId());
        var processes = processResultRepository.findBySchIdxOrderByRegDtDesc(schIdx);
        long success = processes.stream().filter(ProcessResult::getPrcResult).count();
        long fail = processes.size() - success;
        String rate = processes.isEmpty() ? "0.0 %" : String.format("%.1f %%", success * 100.0 / processes.size());

        return new MonitoringSummary(
                schedule.getSchIdx(),
                schedule.getPlantCode(),
                plantName,
                schedule.getSchDate().toString(),
                schedule.getLoadTime(),
                schedule.getSchFacilityId(),
                facilityName,
                schedule.getSchAmount(),
                success,
                fail,
                rate
        );
    }
    
    public ControlMessage start(int schIdx) {
    	Schedule schedule= scheduleRepository.findById(schIdx)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + schIdx));
    	return new ControlMessage(
    		     "MON01",
                 schedule.getPlantCode(),
                 schedule.getSchFacilityId(),
                 DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()),
                 "ON"
          );
    }
    
    @Transactional
    public ProcessResult saveInspectionResult(InspectionResultRequest request) {
    	 Schedule schedule = scheduleRepository.findById(request.schIdx())
                 .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + request.schIdx()));
         boolean ok = "OK".equalsIgnoreCase(request.result());
         String prcCd = DateTimeFormatter.BASIC_ISO_DATE.format(schedule.getSchDate()) + "-" + UUID.randomUUID();
         ProcessResult processResult = new ProcessResult(
                 schedule.getSchIdx(),
                 prcCd,
                 schedule.getSchDate(),
                 schedule.getLoadTime(),
                 schedule.getSchFacilityId(),
                 ok
         );
         return processResultRepository.save(processResult);
    }
    
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
    }
}
