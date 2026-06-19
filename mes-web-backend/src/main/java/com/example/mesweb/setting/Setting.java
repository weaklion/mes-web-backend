package com.example.mesweb.setting;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "settings")
@Getter
public class Setting {
	@Id
	private String basicCode;
	private String codeName;
	private String codeDesc;
	private LocalDateTime regDt;
	private LocalDateTime modDt;
	
	protected Setting() {
	}
	
    public Setting(String basicCode, String codeName, String codeDesc) {
        this.basicCode = basicCode;
        this.codeName = codeName;
        this.codeDesc = codeDesc;
        this.regDt = LocalDateTime.now();
    }
    
    public void update(String codeName, String codeDesc) {
    	this.codeName = codeName;
    	this.codeDesc = codeDesc;
    	this.modDt = LocalDateTime.now();
    }


}