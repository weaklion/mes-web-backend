package com.example.mesweb.monitoring;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.mesweb.monitoring.dto.MonitoringSummary;

@Service
public class MonitoringSseService {
    private final Map<Integer, List<SseEmitter>> sseEmitterMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Integer schIdx) {
        long timeout = 1000L * 60 * 60; // 1 hour

        SseEmitter sseEmitter = new SseEmitter(timeout);
        sseEmitterMap.computeIfAbsent(schIdx, key -> new CopyOnWriteArrayList<>())
                .add(sseEmitter);

        sseEmitter.onCompletion(() -> removeEmitter(schIdx, sseEmitter));
        sseEmitter.onTimeout(() -> {
            removeEmitter(schIdx, sseEmitter);
            sseEmitter.complete();
        });
        sseEmitter.onError(throwable -> {
            removeEmitter(schIdx, sseEmitter);
            sseEmitter.complete();
        });

        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connected")
                    .data("connected"));
        } catch (IOException | IllegalStateException e) {
            removeEmitter(schIdx, sseEmitter);
            sseEmitter.complete();
        }

        return sseEmitter;
    }

    public void sendMonitoringSummary(Integer schIdx, MonitoringSummary summary) {
        List<SseEmitter> emitters = sseEmitterMap.get(schIdx);

        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter sseEmitter : emitters) {
            try {
                sseEmitter.send(
                        SseEmitter.event()
                                .name("monitoring-summary")
                                .data(summary)
                );
            } catch (IOException | IllegalStateException e) {
                removeEmitter(schIdx, sseEmitter);
                sseEmitter.complete();
            }
        }
    }

    private void removeEmitter(Integer schIdx, SseEmitter sseEmitter) {
        List<SseEmitter> emitters = sseEmitterMap.get(schIdx);

        if (emitters == null) {
            return;
        }

        emitters.remove(sseEmitter);

        if (emitters.isEmpty()) {
            sseEmitterMap.remove(schIdx);
        }
    }
}
