# ✈️ Flight Booking System - MSA (Microservices Architecture)

항공편 검색 및 예약 시스템을 마이크로서비스 아키텍처로 구현한 프로젝트입니다.

## 🏗️ 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (Nginx)                     │
│                    http://localhost:8080                    │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│Flight Search│ │Flight Booking│ │  Static     │
│  Service    │ │   Service    │ │  Files      │
│   :8081     │ │    :8082     │ │             │
└─────────────┘ └─────────────┘ └─────────────┘
        │             │
        ▼             ▼
┌─────────────┐ ┌─────────────┐
│Search MySQL │ │Booking MySQL│
│   DB :3307  │ │   DB :3308  │
└─────────────┘ └─────────────┘
```

## 🚀 주요 기능

### 🔍 Flight Search Service (포트: 8081)
- 항공편 검색 (출발지/도착지 기반)
- Amadeus API 연동 (실제 API 호출 또는 Mock 데이터)
- 독립적인 MySQL 데이터베이스

### 📋 Flight Booking Service (포트: 8082)
- 항공편 예약 생성
- 예약 조회 (이메일 + 승객명)
- 예약 취소
- Search Service와 통신 (WebClient)
- 독립적인 MySQL 데이터베이스

### 🌐 API Gateway (포트: 8080)
- Nginx 기반 리버스 프록시
- 라우팅 및 로드밸런싱
- CORS 및 Rate Limiting
- 정적 파일 서빙

## 🛠️ 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring WebFlux** (Inter-service communication)
- **MySQL 8.0**
- **Gradle 8.5**

### Infrastructure
- **Docker & Docker Compose**
- **Nginx** (API Gateway)
- **Microservices Architecture**

### External APIs
- **Amadeus Flight API**

## 📋 사전 요구사항

- Docker & Docker Compose
- Java 17 (개발 시)
- Amadeus API 키 (선택사항)

## 🚀 실행 방법

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd flight-booking
```

### 2. 환경 변수 설정
```bash
# .env 파일 생성 (env-msa.example 참고)
cp env-msa.example .env

# API 키 설정 
vim .env
```

### 3. MSA 시스템 실행
```bash
# 전체 MSA 시스템 빌드 및 실행
docker-compose -f docker-compose-msa.yml up --build

# 백그라운드 실행
docker-compose -f docker-compose-msa.yml up -d --build
```

### 4. 접속 확인
- **Frontend**: http://localhost:8080
- **API Gateway**: http://localhost:8080
- **Flight Search API**: http://localhost:8080/api/flights/
- **Booking API**: http://localhost:8080/api/bookings

## 📡 API 엔드포인트

### Flight Search Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/flights/search-simple` | 간단한 항공편 검색 |
| GET | `/actuator/health` | 서비스 헬스체크 |

### Flight Booking Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | 새 예약 생성 |
| GET | `/api/bookings/search` | 이메일+이름으로 예약 조회 |
| PUT | `/api/bookings/{id}/cancel` | 예약 취소 |
| GET | `/actuator/health` | 서비스 헬스체크 |

## 🔧 개발 환경 설정

### 개별 서비스 실행

#### Flight Search Service
```bash
cd flight-search-service
./gradlew bootRun
```

#### Flight Booking Service
```bash
cd flight-booking-service
./gradlew bootRun
```


## 🌍 환경 변수

```bash
# Amadeus API (선택사항)
AMADEUS_CLIENT_ID=your_amadeus_client_id
AMADEUS_CLIENT_SECRET=your_amadeus_client_secret

# Database Settings
DB_HOST=search-db
SEARCH_DB_HOST=search-db
BOOKING_DB_HOST=booking-db
DB_USERNAME=flight_user
DB_PASSWORD=FlightBooking2024!

# Service URLs
FLIGHT_SEARCH_SERVICE_URL=http://flight-search-service:8081
```

## 🐳 Docker 명령어

```bash
# 전체 시스템 실행
docker-compose -f docker-compose-msa.yml up -d

# 로그 확인
docker-compose -f docker-compose-msa.yml logs -f

# 특정 서비스 로그
docker logs flight-search-service
docker logs flight-booking-service

# 시스템 중지
docker-compose -f docker-compose-msa.yml down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose -f docker-compose-msa.yml down -v
```

## 📊 모니터링

### 헬스체크
- **API Gateway**: http://localhost:8080/health
- **Search Service**: http://localhost:8081/actuator/health
- **Booking Service**: http://localhost:8082/actuator/health

### 서비스 상태 확인
```bash
# 실행 중인 컨테이너 확인
docker ps

# 서비스별 상태 확인
curl http://localhost:8080/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 🔄 MSA 통신 플로우

### 예약 생성 플로우
1. **Frontend** → **API Gateway** (예약 요청)
2. **API Gateway** → **Booking Service**
3. **Booking Service** → **Search Service** (항공편 정보 검증)
4. **Booking Service** → **Booking DB** (예약 저장)
5. **Booking Service** → **API Gateway** → **Frontend** (응답)

## 📝 개발 가이드

### 새로운 기능 추가 시
1. 해당 마이크로서비스에 기능 구현
2. API 문서 업데이트
3. 테스트 코드 작성
4. Docker 이미지 재빌드

### 새로운 마이크로서비스 추가 시
1. 새 서비스 디렉토리 생성
2. `docker-compose-msa.yml`에 서비스 추가
3. `nginx-msa.conf`에 라우팅 규칙 추가
4. 환경 변수 설정
