package com.example.mesweb.monitoring;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.mesweb.inspection.dto.InspectionResultRequest;
import com.example.mesweb.monitoring.dto.ControlMessage;
import com.example.mesweb.monitoring.dto.MonitoringSummary;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class MonitoringController {
	  private final MonitoringSseService monitoringSseService;
	  private final MonitoringService monitoringService;
	  
	    public MonitoringController(MonitoringService monitoringService, MonitoringSseService monitoringSseService) {
	        this.monitoringService = monitoringService;
			this.monitoringSseService = monitoringSseService;
	    }
	    
	    @GetMapping("/monitoring/{schIdx}")
	    public MonitoringSummary summary(@PathVariable("schIdx") int schIdx) {
	        return monitoringService.summary(schIdx);
	    }

	    @PostMapping("/monitoring/{schIdx}/start")
	    public ControlMessage start(@PathVariable("schIdx") int schIdx) {
	        return monitoringService.start(schIdx);
	    }


//	    
	    @GetMapping(value = "/monitoring/{schIdx}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	    public SseEmitter subscribe(@PathVariable("schIdx") int schIdx) {
	        return monitoringSseService.subscribe(schIdx);
	    }

}
