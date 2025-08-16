-- MySQL 초기화 스크립트
-- Search Service와 Booking Service용 데이터베이스 및 사용자 생성

-- 사용자 생성 (이미 존재할 수 있으므로 IF NOT EXISTS 사용)
CREATE USER IF NOT EXISTS 'flight_user'@'%' IDENTIFIED BY 'FlightBooking2024!';

-- Search Service용 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS flight_search_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Booking Service용 데이터베이스 생성  
CREATE DATABASE IF NOT EXISTS flight_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 권한 부여
GRANT ALL PRIVILEGES ON flight_search_db.* TO 'flight_user'@'%';
GRANT ALL PRIVILEGES ON flight_booking_db.* TO 'flight_user'@'%';

-- 권한 적용
FLUSH PRIVILEGES; 