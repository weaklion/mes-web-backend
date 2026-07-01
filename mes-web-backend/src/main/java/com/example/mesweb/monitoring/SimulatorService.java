package com.example.mesweb.monitoring;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mesweb.global.mqtt.MqtttService;
import com.example.mesweb.inspection.dto.InspectionMessage;
import com.example.mesweb.monitoring.dto.SimulatorInspectionRequest;
import com.example.mesweb.schedule.Schedule;
import com.example.mesweb.schedule.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulatorService {
    private final ScheduleRepository scheduleRepository;
    private final MqtttService mqtttService;

    @Transactional(readOnly = true)
    public InspectionMessage publishInspectionResult(SimulatorInspectionRequest request) {
        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + request.scheduleId()));

        InspectionMessage message = new InspectionMessage(
                "EVT-" + UUID.randomUUID(),
                schedule.getSchIdx(),
                request.clientId(),
                schedule.getPlantCode(),
                schedule.getSchFacilityId(),
                request.result(),
                OffsetDateTime.now()
        );

        mqtttService.publishInspectionResult(message);
        return message;
    }
}
