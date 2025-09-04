# 🎬 Ticketory – Movie Reservation Platform

📌 Language: [한국어](#-ticketory--movie-reservation-platform-kr) | [English](#-ticketory--movie-reservation-platform-en)

---

## 🇰🇷 Ticketory – Movie Reservation Platform (KR)

**Ticket + Story = Ticketory**  
3주 동안 4명이 협업하여 개발한 **영화 예매 & 스토리 공유 플랫폼**입니다.  
영화 예매, 결제, 티켓 QR 발급뿐만 아니라 관람평과 스토리 피드를 통해 다른 유저들과 소통할 수 있습니다.  

---

## 🚀 주요 기능

### 🎟️ 사용자 (User)
- 영화 목록 조회, 상세 정보, 예고편 확인
- 좌석 선택 → 예매 → 결제 (수요일 할인, 좌석별 차등 요금 적용)
- 결제 후 **QR 티켓 발급**
- 마이페이지: 회원정보 수정, 예매 내역 확인, 스토리/관람평 관리

### 🛠 관리자 (Admin)
- 영화 CRUD (포스터, 스틸컷, 예고편 등록)
- 상영관, 상영시간 관리
- 프로모션/할인 정책 관리
- 게시판 (공지사항, 이벤트 배너) 관리
- 대시보드: 일별 매출, TOP 영화, 월별 통계

---

## 🖼 기술스택 (Tech stack)
- **Frontend**: React, Vite, Zustand, React Query, TailwindCSS  
- **Backend**: Spring Boot, JPA, JWT authentication, OAuth2 (Kakao login), Spring Batch  
- **Database**: MariaDB (32 tables)  
- **Infra**: Swagger (Testing)  

---

## 📸 최종 화면 (Final Screens)
- 메인 페이지
- 영화 상세보기
- 영화 예매
- 이벤트/공지 게시판
- 좌석예매
- 스토리 피드
- 마이페이지
- 관리자 대시보드
- 관리자 영화관리

---

## 🎥 시연 영상 (Demo Video)
- 예매 흐름 (영화 → 상영시간 → 좌석 → 결제 → QR 티켓)
- 관리자 영화 추가 흐름 (관리자 → 영화관리 → 새 영화 추가 → 미디어 추가 → 홈)

👉 [프로젝트 발표자료 (PDF)](./docs/Ticketory_Presentation.pdf)

---

---

## 🇺🇸 Ticketory – Movie Reservation Platform (EN)

**Ticket + Story = Ticketory**  
A **movie reservation & story-sharing platform** developed by a 4-member team in 3 weeks.  
Users can book movies, make payments, receive QR tickets, and share reviews and stories with others.  

---

## 🚀 Key Features

### 🎟️ User
- Browse movie listings, details, and trailers
- Seat selection → Reservation → Payment (Wednesday discount, seat-based pricing)
- **QR ticket issuance** after payment
- My Page: update profile, view booking history, manage stories/reviews

### 🛠 Admin
- Movie CRUD (poster, still cuts, trailer upload)
- Manage theaters & screening schedules
- Manage promotions & discount policies
- Notice & event board management
- Dashboard: daily revenue, top movies, monthly statistics

---

## 🖼 Tech Stack
- **Frontend**: React, Vite, Zustand, React Query, TailwindCSS  
- **Backend**: Spring Boot, JPA, JWT authentication, OAuth2 (Kakao login), Spring Batch  
- **Database**: MariaDB (32 tables)  
- **Infra**: Swagger (Testing)  

---

## 📸 Final Screens
- Main Page
- Movie Details
- Movie Booking
- Event/Notice Board
- Seat Selection
- Story Feed
- My Page
- Admin Dashboard
- Admin Movie Management

---

## 🎥 Demo Video
- Booking Flow (Movie → Schedule → Seat → Payment → QR Ticket)
- Admin Flow (Admin → Movie Management → Add New Movie → Add Media → Home)

👉 [Project Presentation (PDF)](./docs/Ticketory_Presentation.pdf)
