package com.example.process;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessResultRepository extends JpaRepository<ProcessResult, Integer>{
	List<ProcessResult> findBySchIdxOrderByRegDtDesc(Integer schIdx);

}
