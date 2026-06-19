package com.example.mesweb.monitoring;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mesweb.monitoring.dto.ControlMessage;
import com.example.mesweb.monitoring.dto.InspectionResultRequest;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class MonitoringController {
	  private final MonitoringService monitoringService;
	  
	    public MonitoringController(MonitoringService monitoringService) {
	        this.monitoringService = monitoringService;
	    }
	    
	    @GetMapping("/monitoring/{schIdx}")
	    public MonitoringSummary summary(@PathVariable int schIdx) {
	        return monitoringService.summary(schIdx);
	    }

	    @PostMapping("/monitoring/{schIdx}/start")
	    public ControlMessage start(@PathVariable int schIdx) {
	        return monitoringService.start(schIdx);
	    }

	    @PostMapping("/simulator/inspection-results")
	    public MonitoringSummary inspectionResult(@Valid @RequestBody InspectionResultRequest request) {
	        monitoringService.saveInspectionResult(request);
	        return monitoringService.summary(request.schIdx());
	    }

}
