package com.example.setting;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/settings")
public class SettingController {
	  private final SettingRepository settingRepository;

	    public SettingController(SettingRepository settingRepository) {
	        this.settingRepository = settingRepository;
	    }

	    @GetMapping
	    public List<Setting> list() {
	        return settingRepository.findAll();
	    }
}
