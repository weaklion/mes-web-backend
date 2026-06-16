package com.example.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table (name = "schedules")
@Getter
public class Schedule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer schIdx; //공정계획 번호
	private String plantCode; //공장 생산 코드
	private LocalDate schDate; // 공정계획일
	private Integer loadTime; //제품 처리 시간
	private LocalTime schStartTime; // 계획 시작 시간
	private LocalTime schEndTime; //계획 종료 시간 
	private String schFacilityId; //설비 처리 id
	private Integer schAmount; // 목표 생산 수량
	private LocalDateTime regDt; //등록일시
	private LocalDateTime modDt; //수정일시
	
	 protected Schedule() {
	 }
	 
	public Schedule(String plantCode, LocalDate schDate, Integer loadTime, LocalTime schStartTime,
                LocalTime schEndTime, String schFacilityId, Integer schAmount) {
	    this.plantCode = plantCode;
	    this.schDate = schDate;
	    this.loadTime = loadTime;
	    this.schStartTime = schStartTime;
	    this.schEndTime = schEndTime;
	    this.schFacilityId = schFacilityId;
	    this.schAmount = schAmount;
	    this.regDt = LocalDateTime.now();
	}

	
}
