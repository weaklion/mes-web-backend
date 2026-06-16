package com.example.setting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, String> {
	List<Setting> findByBasicCodeStartingWith(String prefix);
}
