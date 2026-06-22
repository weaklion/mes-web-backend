package com.example.mesweb.process;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "processes")
public class ProcessResult {
	@Id
	@GeneratedValue(strategy =GenerationType.IDENTITY )
	private Integer prcIdx;
	private Integer schIdx; //공정 계획 번호
	private String prcCd; //공정처리 코드
	private LocalDate prcDate; //공정 처리일
	private Integer prcLoadTime; // 처리 시간
	private String prcFacilityId; //처리 설비 id
	private Boolean prcResult; // 공정 결과
	private LocalDateTime regDt; //결과 저장일시
	
	 protected ProcessResult() {
	    }
	 
	 public ProcessResult(Integer schIdx, String prcCd, LocalDate  prcDate, Integer prcLoadTime,
			 String prcFacilityId, Boolean prcResult) {
		 this.schIdx = schIdx;
		 this.prcCd = prcCd;
		 this.prcDate = prcDate;
		 this.prcLoadTime = prcLoadTime;
		 this.prcFacilityId = prcFacilityId;
		 this.prcResult = prcResult;
		 this.regDt = LocalDateTime.now();
	 }

	
}
