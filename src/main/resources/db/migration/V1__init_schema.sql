-- GearShare initial schema

CREATE TABLE users (
    id            CHAR(36)     NOT NULL PRIMARY KEY,
    username      VARCHAR(30)  NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(60)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE equipment (
    id           CHAR(36)      NOT NULL PRIMARY KEY,
    name         VARCHAR(80)   NOT NULL,
    description  VARCHAR(1000) NOT NULL,
    category     VARCHAR(30)   NOT NULL,
    daily_price  DECIMAL(8, 2) NOT NULL,
    location     VARCHAR(80)   NOT NULL,
    available    BOOLEAN       NOT NULL DEFAULT TRUE,
    owner_id     CHAR(36)      NOT NULL,
    CONSTRAINT fk_equipment_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE bookings (
    id           CHAR(36)      NOT NULL PRIMARY KEY,
    equipment_id CHAR(36)      NOT NULL,
    renter_id    CHAR(36)      NOT NULL,
    start_date   DATE          NOT NULL,
    end_date     DATE          NOT NULL,
    total_price  DECIMAL(9, 2) NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    CONSTRAINT fk_booking_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_booking_renter FOREIGN KEY (renter_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE reviews (
    id         CHAR(36)    NOT NULL PRIMARY KEY,
    booking_id CHAR(36)    NOT NULL,
    rating     INT         NOT NULL,
    comment    VARCHAR(500),
    created_on DATETIME    NOT NULL,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT uq_review_booking UNIQUE (booking_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_equipment_owner ON equipment (owner_id);
CREATE INDEX idx_booking_equipment ON bookings (equipment_id);
CREATE INDEX idx_booking_renter ON bookings (renter_id);
