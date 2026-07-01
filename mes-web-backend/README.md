# MES IoT Monitoring Project

Spring Boot, PostgreSQL, MQTT, SSE, Vue 3를 사용한 MES/IoT 토이 프로젝트입니다.

가상 설비에서 발생한 검사 결과를 MQTT 메시지로 처리하고, 백엔드가 이를 저장한 뒤 SSE(Server-Sent Events)로 프론트 모니터링 화면을 실시간 갱신합니다.

## 프로젝트 목표

이 프로젝트의 핵심 목표는 다음 흐름을 구현하는 것입니다.

```text
Vue Line Simulator
-> Spring simulator API
-> MQTT publish
-> Mosquitto Broker
-> Spring MQTT subscriber
-> InspectionMessage 변환
-> MonitoringService.saveInspectionResult()
-> ProcessResult PostgreSQL 저장
-> MonitoringSummary 생성
-> SSE publish
-> Vue Dashboard 실시간 갱신
```

MQTT Explorer를 사용해 직접 MQTT 메시지를 발행해도 같은 수신 흐름을 테스트할 수 있습니다.

## 기술 스택

### Backend

- Java 21
- Spring Boot 4.1.0
- Spring Web / Spring MVC
- Spring Data JPA
- Spring Validation
- Spring Integration MQTT
- Eclipse Paho MQTT Client
- PostgreSQL
- Lombok
- Gradle / STS

### Frontend

- Vue 3
- TypeScript
- axios
- Tailwind CSS
- Vite
- EventSource(SSE)

### Infra

- Mosquitto MQTT Broker
- PostgreSQL
- MQTT Explorer(optional)

## 주요 흐름

### 1. 프론트 시뮬레이터 흐름

Vue 대시보드의 `Start` 버튼을 누르면 Line Simulator가 반복 실행됩니다.

```text
Start 클릭
-> /api/monitoring/{schIdx}/start
-> loadTime 동안 moving 상태
-> /api/simulator/inspection-results/mqtt
-> Spring MQTT publish
-> Mosquitto Broker
-> Spring MQTT subscriber 재수신
-> DB 저장
-> SSE로 Vue 갱신
```

`Stop` 버튼을 누르면 프론트의 반복 타이머가 종료되고 더 이상 검사 이벤트를 발행하지 않습니다.

### 2. MQTT Explorer 테스트 흐름

MQTT Explorer에서 직접 topic과 payload를 발행할 수도 있습니다.

topic:

```text
mes/PLT01/FAC01/inspection
```

payload:

```json
{
  "eventId": "EVT-001",
  "scheduleId": 1,
  "clientId": "IOT01",
  "plantCode": "PLT01",
  "facilityId": "FAC01",
  "result": "OK",
  "inspectedAt": "2026-07-01T14:30:00+09:00"
}
```

백엔드는 topic과 payload의 `plantCode`, `facilityId`가 일치하는지 검증한 뒤 저장합니다.

### 3. SSE 실시간 갱신 흐름

프론트는 모니터링 화면 진입 시 SSE에 연결합니다.

```text
GET /api/monitoring/{schIdx}/events
```

백엔드는 검사 결과 저장 후 최신 `MonitoringSummary`를 `monitoring-summary` 이벤트로 전송합니다.

프론트는 이 이벤트를 받아 OK/FAIL 수량, 성공률, 생산 현황을 갱신합니다.

## 백엔드 구조

```text
src/main/java/com/example/mesweb
├─ global
│  ├─ SeedData.java
│  └─ mqtt
│     ├─ MqttConfig.java
│     └─ MqtttService.java
├─ inspection
│  ├─ InspectionService.java
│  └─ dto
│     └─ InspectionMessage.java
├─ monitoring
│  ├─ MonitoringController.java
│  ├─ MonitoringService.java
│  ├─ MonitoringSseService.java
│  ├─ SimulatorService.java
│  └─ dto
│     ├─ ControlMessage.java
│     ├─ MonitoringSummary.java
│     └─ SimulatorInspectionRequest.java
├─ process
│  ├─ ProcessResult.java
│  └─ ProcessResultRepository.java
├─ schedule
│  ├─ Schedule.java
│  ├─ ScheduleController.java
│  └─ ScheduleRepository.java
└─ setting
   ├─ Setting.java
   ├─ SettingController.java
   └─ SettingRepository.java
```

## 주요 클래스 역할

### `MqttConfig`

- MQTT inbound/outbound 채널 설정
- `mes/+/+/inspection` topic 구독
- MQTT payload를 `InspectionMessage`로 변환
- `InspectionService.process()` 호출

### `MqtttService`

- 백엔드에서 MQTT 메시지를 발행하는 서비스
- 프론트 시뮬레이터 요청을 실제 MQTT 메시지로 publish

### `SimulatorService`

- 프론트 시뮬레이터 요청을 받아 `InspectionMessage` 생성
- schedule 기준으로 `plantCode`, `facilityId`를 채움
- `MqtttService`를 통해 MQTT 발행

### `InspectionService`

- MQTT로 수신된 검사 메시지 처리
- topic과 payload의 설비 정보 검증
- `MonitoringService.saveInspectionResult()` 호출
- 저장 후 `MonitoringSseService.sendMonitoringSummary()` 호출

### `MonitoringService`

- 모니터링 요약 조회
- 검사 결과를 `ProcessResult`로 저장

### `MonitoringSseService`

- schIdx별 SSE 연결 관리
- 여러 브라우저 탭이 같은 schIdx를 구독할 수 있도록 `Map<Integer, List<SseEmitter>>` 사용
- `monitoring-summary` 이벤트 전송

## 주요 API

### 기준정보 조회

```http
GET /api/settings
```

### 생산계획 조회

```http
GET /api/schedules
```

### 모니터링 요약 조회

```http
GET /api/monitoring/{schIdx}
```

예:

```http
GET /api/monitoring/1
```

### 공정 시작 메시지 생성

```http
POST /api/monitoring/{schIdx}/start
```

응답 예:

```json
{
  "clientId": "MON01",
  "plantCode": "PLT01",
  "facilityId": "FAC01",
  "timestamp": "2026-07-01 14:30:00",
  "flag": "ON"
}
```

### 프론트 시뮬레이터 MQTT 발행

```http
POST /api/simulator/inspection-results/mqtt
Content-Type: application/json
```

요청 예:

```json
{
  "scheduleId": 1,
  "clientId": "IOT01",
  "result": "OK"
}
```

응답 예:

```json
{
  "eventId": "EVT-5d0f8b51-3f8e-4bb2-9127-a6328f7b6b89",
  "scheduleId": 1,
  "clientId": "IOT01",
  "plantCode": "PLT01",
  "facilityId": "FAC01",
  "result": "OK",
  "inspectedAt": "2026-07-01T14:30:00+09:00"
}
```

이 API는 DB에 직접 저장하지 않습니다. MQTT로 publish한 뒤, 같은 백엔드의 MQTT subscriber가 다시 수신하여 저장합니다.

### SSE 연결

```http
GET /api/monitoring/{schIdx}/events
```

이벤트 이름:

```text
connected
monitoring-summary
```

## MQTT 설정

`src/main/resources/application.yml`

```yaml
mqtt:
  url: tcp://localhost:1883
  client-id: spring-mqtt-app
  topic: "mes/+/+/inspection"
```

백엔드는 다음 topic 패턴만 구독합니다.

```text
mes/{plantCode}/{facilityId}/inspection
```

outbound clientId는 inbound와 충돌하지 않도록 `spring-mqtt-app-outbound` 형태로 분리했습니다.

## PostgreSQL 설정

`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sdmes
    username: mesuser
    password: mesuser
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
    show-sql: true
```

초기 DB 생성 예:

```sql
CREATE USER mesuser WITH PASSWORD 'mesuser';
CREATE DATABASE sdmes OWNER mesuser;
ALTER DATABASE sdmes SET timezone TO 'Asia/Seoul';
GRANT ALL PRIVILEGES ON DATABASE sdmes TO mesuser;
GRANT USAGE, CREATE ON SCHEMA public TO mesuser;
```

## 샘플 데이터

`SeedData.java`에서 서버 시작 시 데이터가 없으면 샘플 데이터를 생성합니다.

기준정보:

- `PLT01`: Assembly Plant A
- `FAC01`: Conveyor Inspector 01
- `FAC02`: Conveyor Inspector 02

생산계획:

- plantCode: `PLT01`
- facilityId: `FAC01`
- target amount: `20`
- loadTime: `3`

## 실행 방법

### 1. PostgreSQL 실행

로컬 PostgreSQL이 실행 중이어야 합니다.

기본 접속 정보:

```text
host: localhost
port: 5432
database: sdmes
username: mesuser
password: mesuser
```

### 2. Mosquitto 실행

MQTT Broker가 `tcp://localhost:1883`에서 실행 중이어야 합니다.

### 3. 백엔드 실행

STS에서 `MesWebBackendApplication.java`를 Spring Boot App으로 실행합니다.

또는 Gradle 프로젝트 구성이 정상화된 환경에서는 다음 명령을 사용할 수 있습니다.

```bash
gradlew.bat bootRun
```

### 4. 프론트 실행

프론트 프로젝트 경로:

```text
C:\Users\weakl\Documents\dev\mes-web-frontend
```

실행:

```bash
npm install
npm run dev
```

기본 주소:

```text
http://localhost:5173
```

## 테스트 순서

1. PostgreSQL 실행
2. Mosquitto 실행
3. Spring Boot 백엔드 실행
4. Vue 프론트 실행
5. 브라우저에서 `http://localhost:5173` 접속
6. Schedule ID를 `1`로 둔 상태에서 `Start` 클릭
7. Line Simulator가 반복 실행되는지 확인
8. OK/FAIL 수량과 성공률이 SSE로 자동 갱신되는지 확인
9. `Stop` 클릭 시 반복 실행이 중단되는지 확인

### MQTT 메시지가 저장되지 않는 경우

확인할 것:

- Mosquitto가 `1883` 포트에서 실행 중인지
- topic이 `mes/PLT01/FAC01/inspection` 형태인지
- payload의 `plantCode`, `facilityId`가 topic과 일치하는지
- `scheduleId`가 DB에 존재하는지

## 현재 구현 범위

- MQTT 수신 처리
- 프론트 시뮬레이터에서 MQTT publish 트리거
- PostgreSQL 검사 결과 저장
- SSE 실시간 모니터링 갱신
- 반복 Start / Stop 시뮬레이터 UI
- Schedule / Setting 조회

## 향후 개선 아이디어

- 최근 검사 이력 API 추가
- 설비별 상세 모니터링 화면 추가
- MQTT publish 실패 시 프론트 에러 메시지 강화
- 테스트 코드 추가
- Gradle 프로젝트 구조 정리
- 실제 설비 MQTT payload와 호환성 검증
