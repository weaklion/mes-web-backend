package com.example.global;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.schedule.Schedule;
import com.example.schedule.ScheduleRepository;
import com.example.setting.Setting;
import com.example.setting.SettingRepository;

@Configuration
public class SeedData {
	@Bean
	CommandLineRunner seed(SettingRepository settings, ScheduleRepository schedules) {
		return args -> {
			 if (settings.count() == 0) {
	                settings.save(new Setting("PLT01", "Assembly Plant A", "Main virtual plant"));
	                settings.save(new Setting("FAC01", "Conveyor Inspector 01", "Sorting and inspection cell"));
	                settings.save(new Setting("FAC02", "Conveyor Inspector 02", "Backup inspection cell"));
	            }
	            if (schedules.count() == 0) {
	                schedules.save(new Schedule("PLT01", LocalDate.now(), 3, LocalTime.of(9, 0),
	                        LocalTime.of(18, 0), "FAC01", 20));
	            }
		};
	}


}
