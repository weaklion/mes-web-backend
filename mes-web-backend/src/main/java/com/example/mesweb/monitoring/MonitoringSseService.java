package com.example.mesweb.monitoring;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class MonitoringSseService {
	private final Map<Integer, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
			
	public SseEmitter subscribe(Integer schIdx) { 
	    long timeout = 1000L * 60 * 60; // sse emitter 연결 시간, 1시간
	   
	   
	    SseEmitter sseEmitter = new SseEmitter(timeout);
	    sseEmitterMap.put(schIdx, sseEmitter);
	    
	    sseEmitter.onCompletion(() -> sseEmitterMap.remove(schIdx));
        sseEmitter.onTimeout(() -> {
            sseEmitterMap.remove(schIdx);
            sseEmitter.complete();
        });
        sseEmitter.onError(throwable -> {
            sseEmitterMap.remove(schIdx);
            sseEmitter.complete();
        });

	    
	    return sseEmitter;
	}
	
	public void sendInspectionResult(Integer schIdx, Object data) {
		SseEmitter sseEmitter  = sseEmitterMap.get(schIdx);
		
		if(sseEmitter ==null) {
			return ;
		}
		
		try {
			sseEmitter.send(SseEmitter.event().name("inspection-result").data(data));
		}  catch (IOException | IllegalStateException e) {
			sseEmitterMap.remove(schIdx);
            sseEmitter.complete();
		}
	}
}
