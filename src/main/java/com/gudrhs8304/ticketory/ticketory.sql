/* 1) 상영관 */
CREATE TABLE screen
(
    screen_id   BIGINT PRIMARY KEY AUTO_INCREMENT, -- 상영관 고유 ID
    name VARCHAR(50) NOT NULL,              -- 상영관 이름
    row_count INT         NOT NULL,              -- 좌석 행 개수
    col_count INT         NOT NULL,              -- 좌석 열 개수
    is_active TINYINT(1) NOT NULL DEFAULT 1,       -- 사용 여부(비활성화 시 목록/편성 제외)
    location    VARCHAR(255) null,
    description text null,
    UNIQUE KEY uk_screen_name (name)        -- 상영관 이름 중복 방지: 같은 이름의 상영관을 한 번만 허용
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 2) 좌석 */
CREATE TABLE seat
(
    seat_id         BIGINT PRIMARY KEY AUTO_INCREMENT,                            -- 좌석 고유 ID
    screen_id  BIGINT NOT NULL,                                              -- 소속 상영관 ID
    row_label  VARCHAR    NOT NULL,                                              -- 좌석 행 번호
    col_number INT    NOT NULL,                                              -- 좌석 열 번호
    seat_type   ENUM('NORMAL', 'VIP')       DEFAULT 'NORMAL' ,
    status  ENUM('AVAILABLE', 'PENDING')    DEFAULT 'AVAILABLE',
    UNIQUE KEY uk_seat_position (screen_id, row_label, col_number),         -- 같은 상영관 내에서 좌석 좌표(행,열)는 유일
    CONSTRAINT fk_seat_screen FOREIGN KEY (screen_id) REFERENCES screen (screen_id) -- 좌석은 반드시 특정 상영관에 속함
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 3) 영화 */
CREATE TABLE movie
(
    movie_id              BIGINT PRIMARY KEY AUTO_INCREMENT, -- 영화 고유 ID
    title           VARCHAR(200) NOT NULL,             -- 영화 제목
    genre           VARCHAR(100),                      -- 영화 장르
    release_date    DATE,
    rating          varchar(50) null ,
    running_minutes INT          NOT NULL,             -- 상영 시간(분)
    director        VARCHAR(100),                      -- 감독명
    actors          text,                      -- 출연 배우 목록
    summary         TEXT,                              -- 영화 줄거리
    status          Boolean NOT NULL DEFAULT 1          -- 운영상태
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 4) 상영 일정 */
CREATE TABLE screening
(
    screening_id         BIGINT PRIMARY KEY AUTO_INCREMENT,                                             -- 상영 고유 ID
    movie_id   BIGINT                                           NOT NULL,                     -- 상영 영화 ID
    screen_id  BIGINT                                           NOT NULL,                     -- 상영관 ID
    start_at   DATETIME                                         NOT NULL,                     -- 상영 시작 시간
    end_at     DATETIME                                         NOT NULL,                     -- 상영 종료 시간
    KEY idx_screen_time (screen_id, start_at),                                                -- 상영관/일자별 시간표 빠른 조회용 인덱스
    KEY idx_movie_time (movie_id, start_at),                                                  -- 영화/일자별 상영 목록 빠른 조회용 인덱스
    CONSTRAINT fk_screening_movie FOREIGN KEY (movie_id) REFERENCES movie (movie_id)                -- 상영은 특정 영화에 종속
    ,
    CONSTRAINT fk_screening_screen FOREIGN KEY (screen_id) REFERENCES screen (screen_id)             -- 상영은 특정 상영관에서 진행
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 5) 상영 좌석 상태 */
CREATE TABLE seat_status
(
    seat_status_id            BIGINT PRIMARY KEY AUTO_INCREMENT,                                -- 상영 좌석 고유 ID
    screening_id  BIGINT                                NOT NULL,                   -- 상영 ID
    seat_id       BIGINT                                NOT NULL,                   -- 좌석 ID
    status        ENUM ('AVAILABLE','PENDING','BOOKED') NOT NULL,                   -- 좌석 상태
    pending_until DATETIME                              NULL,                       -- 예약 대기 만료 시간
    UNIQUE KEY uk_screening_seat (screening_id, seat_id),                           -- 같은 상영에서 동일 좌석은 한 번만 생성
    KEY idx_screening_status (screening_id, status),                                -- 상영별 좌석 상태(잔여석 계산/좌석도 표시) 조회 최적화
    CONSTRAINT fk_ss_screening FOREIGN KEY (screening_id) REFERENCES screening (screening_id) -- 상영 좌석은 특정 상영에 속함
    ,
    CONSTRAINT fk_ss_seat FOREIGN KEY (seat_id) REFERENCES seat (seat_id)                -- 상영 좌석이 참조하는 실제 좌석
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 6) 회원 */
CREATE TABLE member
(
    member_id                BIGINT PRIMARY KEY AUTO_INCREMENT,         -- 사용자 고유 ID
    name              VARCHAR(100)           NOT NULL,           -- 사용자 이름
    login_id          VARCHAR(150)           NOT NULL UNIQUE,    -- 로그인 아이디(이메일 또는 카카오 계정 id)
    email             VARCHAR(150)           NULL,               -- 이메일 주소(선택)
    password     VARCHAR(255)           NULL,                    -- 해시 비밀번호(LOCAL 전용)
    phone             VARCHAR(30)            NULL,               -- 전화번호
    signup_type       ENUM ('LOCAL','KAKAO') NOT NULL,           -- 가입 방식
    role              ENUM('USER','ADMIN')     NOT NULL DEFAULT 'USER', -- 권한(관리자 화면 접근 제어)
    social_id         VARCHAR(200)           NULL UNIQUE,        -- 소셜 로그인 고유 ID(KAKAO 전용)
    profile_image_url VARCHAR(300)           NULL,               -- 프로필 이미지 경로
    point_balance     INT                    NOT NULL DEFAULT 0, -- (선택) 포인트 잔액
    created_at        DATETIME               NOT NULL,           -- 가입일
    updated_at        DATETIME               NOT NULL,           -- 수정일
    INDEX idx_member_phone (phone)                               -- 전화번호 조회 최적화
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 7) 예약 */
CREATE TABLE reservation
(
    reservation_id            BIGINT PRIMARY KEY AUTO_INCREMENT,                                  -- 예약 고유 ID
    screening_id  BIGINT                                  NOT NULL,                   -- 상영 ID
    member_id     BIGINT                                  NULL,                       -- 회원 ID(비회원은 NULL)
    guest_name    VARCHAR(100)                            NULL,                       -- 비회원 예약자 이름
    guest_phone   VARCHAR(30)                             NULL,                       -- 비회원 예약자 전화번호
    contact_phone VARCHAR(30)                             NOT NULL,                   -- 연락처 전화번호(회원/비회원 공통)
    status        ENUM ('PENDING','CONFIRMED','CANCELED') NOT NULL,                   -- 예약 상태
    total_price   INT                                     NOT NULL,                   -- 총 결제 금액
    qr_code       VARCHAR(64) UNIQUE,                                                 -- 예약 QR 코드
    cancel_token  VARCHAR(64) UNIQUE,                                                 -- 예약 취소 토큰
    created_at    DATETIME                                NOT NULL,                   -- 예약 생성 일시
    canceled_at   DATETIME                                NULL,                       -- 예약 취소 일시
    KEY idx_resv_screen_status (screening_id, status),                                -- 상영별/상태별 예약 현황 조회 최적화
    KEY idx_resv_member (member_id),                                                  -- 회원의 예약내역 빠른 조회
    KEY idx_resv_contact_phone_created (contact_phone, created_at),                   -- 전화번호 기준 최신 예약 검색/재발송 최적화
    KEY idx_resv_member_created (member_id, created_at),                -- 마이페이지 최신순 조회 최적화
    CONSTRAINT fk_resv_screening FOREIGN KEY (screening_id) REFERENCES screening (screening_id) -- 예약은 특정 상영에 종속
    ,
    CONSTRAINT fk_resv_member FOREIGN KEY (member_id) REFERENCES member (member_id)          -- (옵션) 예약한 회원과의 관계
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 8) 예약 좌석 */
CREATE TABLE reservation_seat
(
    reservation_seat_id                BIGINT PRIMARY KEY AUTO_INCREMENT,                                  -- 예약 좌석 고유 ID
    reservation_id    BIGINT NOT NULL,                                                    -- 예약 ID
    screening_seat_id BIGINT NOT NULL,                                                    -- 상영 좌석 ID
    UNIQUE KEY uk_resv_item (reservation_id, screening_seat_id),                          -- 한 예약에 같은 상영좌석을 중복 추가 금지
    CONSTRAINT fk_rseat_resv FOREIGN KEY (reservation_id) REFERENCES reservation (reservation_id)     -- 좌석들은 특정 예약에 속함
    ,
    CONSTRAINT fk_rseat_ss FOREIGN KEY (screening_seat_id) REFERENCES seat_status (seat_status_id) -- 예약 좌석이 참조하는 상영좌석
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 9) 결제 */
CREATE TABLE payment
(
    payment_id             BIGINT PRIMARY KEY AUTO_INCREMENT,                                       -- 결제 고유 ID
    reservation_id BIGINT                                                  NOT NULL UNIQUE, -- 예약 ID
    method         ENUM ('CARD','TOSS','POINT','MIXED')                    NOT NULL,        -- 결제 수단
    status         ENUM ('INIT','PAID','FAILED','REFUND','REFUND_PENDING') NOT NULL,        -- 결제 상태
    amount         INT                                                     NOT NULL,        -- 결제 금액
    pg_tid         VARCHAR(100)                                            NULL,            -- PG사 거래 ID
    paid_at        DATETIME                                                NULL,            -- 결제 완료 일시
    canceled_at    DATETIME                                                NULL,            -- 결제 취소 일시
    CONSTRAINT fk_payment_resv FOREIGN KEY (reservation_id) REFERENCES reservation (reservation_id)     -- 결제는 예약과 1:1 관계
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 10) SMS 발송 로그 */
CREATE TABLE sms_log
(
    sms_log_id              BIGINT PRIMARY KEY AUTO_INCREMENT,                                        -- SMS 로그 고유 ID
    reservation_id  BIGINT                                        NULL,                       -- 예약 ID
    to_phone        VARCHAR(30)                                   NOT NULL,                   -- 수신 전화번호
    message         TEXT                                          NOT NULL,                   -- 전송 메시지 내용
    sms_type        ENUM ('CONFIRM','CANCEL','REFUND','REMINDER') NOT NULL DEFAULT 'CONFIRM', -- SMS 유형
    status          ENUM ('READY','SENT','FAILED')                NOT NULL DEFAULT 'READY',   -- 전송 상태
    provider_msg_id VARCHAR(100)                                  NULL,                       -- SMS 제공자 메시지 ID
    sent_at         DATETIME                                      NULL,                       -- 전송 일시
    fail_reason     VARCHAR(255)                                  NULL,                       -- 실패 사유
    UNIQUE KEY uk_sms_once (reservation_id, to_phone, sms_type),                              -- 동일 예약/번호/유형에 대해 1회만 기록(중복 발송 방지)
    KEY idx_sms_to_phone (to_phone),                                                          -- 전화번호로 최근 발송 내역 조회 최적화
    CONSTRAINT fk_sms_resv FOREIGN KEY (reservation_id) REFERENCES reservation (reservation_id)           -- 문자 로그와 예약 연결
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* 11) 영화 미디어 (포스터, 스틸컷, 배경, 예고편/클립 등) */
CREATE TABLE movie_media
(
    media_id              BIGINT PRIMARY KEY AUTO_INCREMENT,                 -- 미디어 고유 ID
    movie_id        BIGINT           NOT NULL,                         -- 영화 FK
    media_type      ENUM('POSTER','BACKDROP','STILL','TRAILER','TEASER','CLIP')
                                     NOT NULL,                         -- 미디어 유형
    url             VARCHAR(500)    NOT NULL,                          -- 원본/재생 URL(또는 경로)
    description     varchar(255)    null

    CONSTRAINT fk_movie_media_movie FOREIGN KEY (movie_id) REFERENCES movie(movie_id),

    -- 한 영화에서 같은 URL 중복 방지
    UNIQUE KEY uk_movie_media_url (movie_id, url),

    -- 유형/정렬 조회 최적화
    KEY idx_movie_media_type_sort (movie_id, media_type),

    -- 대표 미디어 빠른 조회
    KEY idx_movie_media_primary (movie_id, media_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 12) 예매/취소 정책 (이력형) */
CREATE TABLE reservation_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 정책 고유 ID
    bookable_minutes_before_start INT NOT NULL DEFAULT 30,   -- 상영 시작 몇 분 전까지 예매 가능
    cancelable_minutes_before_start INT NOT NULL DEFAULT 30, -- 상영 시작 몇 분 전까지 취소 가능
    refund_policy ENUM('FULL','PARTIAL','NONE') NOT NULL DEFAULT 'FULL', -- 환불 정책(전액/부분/불가)
    note VARCHAR(255) NULL, -- 정책 설명 또는 비고
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE', -- 정책 활성/비활성 상태
    effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 정책 효력 시작일시
    effective_to DATETIME NULL, -- 정책 효력 종료일시(미래 폐기 예약 등)
    created_by BIGINT NULL,                                  -- 정책 생성 관리자(member.id)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 생성일시
    CONSTRAINT fk_resvpol_created_by FOREIGN KEY (created_by) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 13) 좌석 홀드 정책 (이력형) */
CREATE TABLE seat_hold (
    hold_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 정책 고유 ID
    user_id BIGINT null ,
    screening_id BIGINT AUTO_INCREMENT,
    seat_id BIGINT NOT NULL ,
    status ENUM('HOLD','EXPIRED') DEFAULT 'HOLD'
    CONSTRAINT fk_holdpol_created_by FOREIGN KEY (created_by) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 14) 시스템 설정 (Key-Value) */
CREATE TABLE system_config (
    config_key VARCHAR(100) PRIMARY KEY, -- 설정 키(예: 'SMS_ENABLED')
    config_value VARCHAR(500) NOT NULL,  -- 설정 값
    description VARCHAR(255) NULL,       -- 설정 설명
    updated_by BIGINT NULL,              -- 마지막 수정 관리자(member.id)
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,     -- 마지막 수정 일시
    CONSTRAINT fk_syscfg_updated_by FOREIGN KEY (updated_by) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 15) 관리자 활동 로그 (감사로그) */
CREATE TABLE admin_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 로그 고유 ID
    admin_member_id BIGINT NOT NULL,                         -- 관리자(member.id)
    action_type ENUM(
        'LOGIN','LOGOUT',
        'CREATE_MOVIE','UPDATE_MOVIE','DELETE_MOVIE',
        'CREATE_SCREENING','UPDATE_SCREENING','DELETE_SCREENING',
        'UPLOAD_MEDIA','DELETE_MEDIA',
        'CANCEL_RESERVATION','REFUND_PAYMENT',
        'UPDATE_CONFIG','UPDATE_POLICY'
    ) NOT NULL, -- 관리자 수행 작업 유형
    target_table VARCHAR(100) NULL,                          -- 작업 대상 테이블(예: 'movie','screening')
    target_id BIGINT NULL,                                   -- 작업 대상 PK
    payload_json JSON NULL,                                  -- 작업 상세(변경 전/후 값 등)
    ip_address VARCHAR(45) NULL,                             -- 관리자 IP 주소
    user_agent VARCHAR(255) NULL,                            -- 관리자 브라우저/클라이언트 정보
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 작업 일시
    CONSTRAINT fk_adminlog_admin FOREIGN KEY (admin_member_id) REFERENCES member(member_id),
    KEY idx_adminlog_created (created_at),
    KEY idx_adminlog_action (action_type, target_table, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 16) 취소 기록(사유/주체 보관) */
CREATE TABLE cancel_log (
    calcel_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 취소 로그 고유 ID
    reservation_id BIGINT NOT NULL, -- 취소된 예약 ID
    canceled_by_member_id BIGINT NULL,  -- 사용자가 직접 취소한 경우(member.id)
    canceled_by_admin_id BIGINT NULL,   -- 관리자가 취소한 경우(member.id)
    reason VARCHAR(255) NULL,           -- 취소 사유
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 취소 처리 일시
    CONSTRAINT fk_rcl_resv FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id),
    CONSTRAINT fk_rcl_member FOREIGN KEY (canceled_by_member_id) REFERENCES member(member_id),
    CONSTRAINT fk_rcl_admin FOREIGN KEY (canceled_by_admin_id) REFERENCES member(member_id),
    KEY idx_rcl_resv_time (reservation_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 17) 환불 기록(부분/재시도 등 이력 보존) */
CREATE TABLE refund_log (
    refund_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 환불 로그 고유 ID
    payment_id BIGINT NOT NULL,           -- 환불 대상 결제 ID
    refund_amount INT NOT NULL,           -- 환불 금액
    reason VARCHAR(255) NULL,             -- 환불 사유
    pg_refund_tid VARCHAR(100) NULL,      -- PG사 환불 거래 ID
    status ENUM('REQUESTED','DONE','FAILED') NOT NULL DEFAULT 'REQUESTED', -- 환불 처리 상태
    processed_by_admin_id BIGINT NULL,    -- 환불 처리 관리자(member.id)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 환불 처리 일시
    CONSTRAINT fk_prl_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id),
    CONSTRAINT fk_prl_admin FOREIGN KEY (processed_by_admin_id) REFERENCES member(member_id),
    KEY idx_prl_payment (payment_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 18) SMS 템플릿 (관리자 수정 가능) */
CREATE TABLE sms_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 템플릿 고유 ID
    template_key VARCHAR(50) NOT NULL UNIQUE,    -- 템플릿 키(예: 'CONFIRM','CANCEL','REFUND')
    template_text TEXT NOT NULL,                 -- 메시지 본문 (변수 치환 규칙은 앱에서)
    enabled TINYINT(1) NOT NULL DEFAULT 1,       -- 사용 여부(비활성화 시 미사용)
    updated_by BIGINT NULL,                      -- 마지막 수정 관리자(member.id)
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,             -- 마지막 수정 일시
    CONSTRAINT fk_smstpl_updated_by FOREIGN KEY (updated_by) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
