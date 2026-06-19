package com.example.mesweb.schedule;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
	 private final ScheduleRepository scheduleRepository;

	    public ScheduleController(ScheduleRepository scheduleRepository) {
	        this.scheduleRepository = scheduleRepository;
	    }

	    @GetMapping
	    public List<Schedule> list() {
	        return scheduleRepository.findAll();
	    }

}
