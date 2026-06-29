# MES Web Backend

Spring Boot 기반의 MES 공정 모니터링 백엔드입니다.

가상 생산 라인의 생산 계획, 기준정보, 검사 결과를 관리하고, MQTT로 들어오는 설비 검사 데이터를 받아 DB에 저장하는 구조를 목표로 합니다. 프론트엔드는 Vue 개발 서버에서 REST API를 호출해 모니터링 데이터를 조회합니다.

## 기술 스택

- Java 21
- Spring Boot 4.1.0
- Spring Web
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- Spring Integration
- Spring Integration MQTT
- Eclipse Paho MQTT Client
- PostgreSQL
- Lombok
- Gradle

## 프로젝트 구조

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
│     ├─ InspectionMessage.java
│     └─ InspectionResultRequest.java
├─ monitoring
│  ├─ MonitoringController.java
│  ├─ MonitoringService.java
│  ├─ MonitoringSummary.java
│  └─ dto
│     └─ ControlMessage.java
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

현재 구조는 도메인별 패키지 방식입니다. 예를 들어 `schedule` 패키지 안에는 생산 계획 엔티티, 컨트롤러, 레포지터리가 함께 들어 있습니다.

## 핵심 흐름

### 1. 모니터링 조회

```text
Vue Frontend
→ GET /api/monitoring/{schIdx}
→ MonitoringController
→ MonitoringService.summary()
→ ScheduleRepository / SettingRepository / ProcessResultRepository
→ MonitoringSummary 응답
```

`MonitoringService.summary()`는 특정 생산 계획 번호(`schIdx`)를 기준으로 계획 정보, 공장명, 설비명, 검사 성공 수, 검사 실패 수, 성공률을 계산해서 반환합니다.

### 2. REST 검사 결과 저장

```text
Vue Frontend 또는 REST 테스트 도구
→ POST /api/simulator/inspection-results
→ MonitoringController
→ MonitoringService.saveInspectionResult()
→ ProcessResult 저장
→ 최신 MonitoringSummary 응답
```

이 방식은 MQTT 없이 HTTP 요청으로 검사 결과를 저장할 때 사용합니다.

### 3. MQTT 검사 결과 저장

```text
MQTT Explorer 또는 가상 설비
→ MQTT Broker
→ MqttConfig.inbound()
→ mqttInputChannel
→ MqttConfig.messageHandler()
→ InspectionMessage 변환
→ InspectionService.process()
→ ProcessResult 저장
```

MQTT payload는 `InspectionMessage` DTO로 변환됩니다. 이후 `InspectionService`에서 생산 계획을 조회하고 검사 결과를 `processes` 테이블에 저장합니다.

## 도메인 설명

### Setting

공장, 설비 같은 기준정보입니다.

| 필드 | 의미 |
| --- | --- |
| `basicCode` | 기준정보 코드. 예: `PLT01`, `FAC01` |
| `codeName` | 기준정보 이름 |
| `codeDesc` | 기준정보 설명 |
| `regDt` | 등록일시 |
| `modDt` | 수정일시 |

### Schedule

생산 계획입니다.

| 필드 | 의미 |
| --- | --- |
| `schIdx` | 생산 계획 번호. DB에서 자동 증가 |
| `plantCode` | 공장 코드 |
| `schDate` | 생산 계획일 |
| `loadTime` | 제품 처리 시간 |
| `schStartTime` | 계획 시작 시간 |
| `schEndTime` | 계획 종료 시간 |
| `schFacilityId` | 처리 설비 코드 |
| `schAmount` | 목표 생산 수량 |
| `regDt` | 등록일시 |
| `modDt` | 수정일시 |

### ProcessResult

실제 공정 검사 결과입니다.

| 필드 | 의미 |
| --- | --- |
| `prcIdx` | 공정 처리 번호. DB에서 자동 증가 |
| `schIdx` | 연결된 생산 계획 번호 |
| `prcCd` | 공정 처리 코드. MQTT에서는 `eventId`를 저장 |
| `prcDate` | 공정 처리일 |
| `prcLoadTime` | 처리 시간 |
| `prcFacilityId` | 처리 설비 코드 |
| `prcResult` | 검사 결과. `true`는 OK, `false`는 FAIL |
| `regDt` | 결과 저장일시 |

## 주요 API

### 기준정보 조회

```http
GET /api/settings
```

공장, 설비 기준정보 목록을 조회합니다.

### 생산 계획 조회

```http
GET /api/schedules
```

생산 계획 목록을 조회합니다.

### 모니터링 요약 조회

```http
GET /api/monitoring/{schIdx}
```

예시:

```http
GET /api/monitoring/1
```

### 공정 시작 메시지 생성

```http
POST /api/monitoring/{schIdx}/start
```

예시:

```http
POST /api/monitoring/1/start
```

응답 예시:

```json
{
  "clientId": "MON01",
  "plantCode": "PLT01",
  "facilityId": "FAC01",
  "timestamp": "2026-06-26 14:30:00",
  "flag": "ON"
}
```

현재는 공정 시작용 제어 메시지를 생성하는 단계입니다.

### REST 검사 결과 저장

```http
POST /api/simulator/inspection-results
Content-Type: application/json
```

요청 예시:

```json
{
  "schIdx": 1,
  "clientId": "IOT01",
  "result": "OK",
  "timestamp": "2026-06-26T14:30:00+09:00"
}
```

응답은 저장 후 최신 모니터링 요약입니다.

## MQTT 설정

MQTT 설정은 `src/main/resources/application.yml`에 있습니다.

```yaml
mqtt:
  url: tcp://localhost:1883
  client-id: spring-mqtt-app
  topic: "mes/#"
```

현재 설정은 `mes/#` 아래의 모든 토픽을 구독합니다.

공장/설비/검사 토픽만 받고 싶다면 아래처럼 좁힐 수 있습니다.

```yaml
mqtt:
  topic: "mes/+/+/inspection"
```

토픽 예시:

```text
mes/PLT01/FAC01/inspection
```

MQTT Explorer에서 보낼 payload 예시:

```json
{
  "eventId": "EVT-20260626-001",
  "scheduleId": 1,
  "clientId": "IOT01",
  "plantCode": "PLT01",
  "facilityId": "FAC01",
  "result": "OK",
  "inspectedAt": "2026-06-26T14:30:00+09:00"
}
```

`InspectionMessage`의 필드명과 JSON key가 같아야 합니다.

## PostgreSQL 설정

현재 DB 설정은 PostgreSQL 기준입니다.

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

server:
  port: 8080
```

DB와 사용자가 없다면 PostgreSQL 관리자 계정에서 아래 SQL을 실행합니다.

```sql
CREATE USER mesuser WITH PASSWORD 'mesuser';
CREATE DATABASE sdmes OWNER mesuser;
ALTER DATABASE sdmes SET timezone TO 'Asia/Seoul';
```

권한 문제가 생기면 아래 SQL도 실행합니다.

```sql
GRANT ALL PRIVILEGES ON DATABASE sdmes TO mesuser;
GRANT USAGE, CREATE ON SCHEMA public TO mesuser;
```

## 샘플 데이터

`SeedData.java`에서 서버 시작 시 샘플 데이터를 생성합니다.

기준정보:

- `PLT01`: Assembly Plant A
- `FAC01`: Conveyor Inspector 01
- `FAC02`: Conveyor Inspector 02

생산 계획:

- 오늘 날짜 기준
- 공장: `PLT01`
- 설비: `FAC01`
- 목표 수량: `20`

이미 데이터가 있으면 중복 저장하지 않습니다.

## 실행 방법

### 1. PostgreSQL 실행

로컬 PostgreSQL이 켜져 있어야 합니다.

기본 접속 정보:

- host: `localhost`
- port: `5432`
- database: `sdmes`
- username: `mesuser`
- password: `mesuser`

### 2. MQTT Broker 실행

MQTT 기능을 테스트하려면 로컬 MQTT Broker가 필요합니다.

예시:

```text
tcp://localhost:1883
```

Mosquitto를 사용한다면 1883 포트로 실행하면 됩니다.

### 3. 백엔드 실행

Windows:

```bash
gradlew.bat bootRun
```

Mac/Linux:

```bash
./gradlew bootRun
```

STS에서는 `MesWebBackendApplication.java`를 Spring Boot App으로 실행합니다.

## 프론트엔드 연동

프론트엔드는 아래 백엔드 주소를 호출합니다.

```text
http://localhost:8080/api
```

현재 CORS는 Vue 개발 서버 주소를 허용합니다.

```java
@CrossOrigin(origins = "http://localhost:5173")
```

Vue 개발 서버는 보통 아래 주소로 실행됩니다.

```text
http://localhost:5173
```

## 현재 개발 상태 메모

- REST 방식 검사 결과 저장은 `MonitoringService.saveInspectionResult()`에서 처리합니다.
- MQTT 방식 검사 결과 저장은 `InspectionService.process()`에서 처리합니다.
- `MqtttService.publish()`는 현재 `"hello mqtt"`를 발행하는 테스트용 코드입니다.
- 프론트엔드는 DB나 MQTT를 직접 알 필요가 없습니다. Spring API를 호출해서 현재 모니터링 상태만 받으면 됩니다.
- 외부 설비 데이터가 들어왔을 때 화면을 갱신하는 방식은 우선 polling으로 시작하고, 이후 SSE 또는 WebSocket으로 확장할 수 있습니다.

