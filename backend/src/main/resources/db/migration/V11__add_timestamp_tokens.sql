CREATE TABLE timestamp_tokens (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    signature_id    BIGINT       NOT NULL,
    tsa_url         VARCHAR(500) NOT NULL,
    serial_number   VARCHAR(100) NOT NULL,
    gen_time        DATETIME(6)  NOT NULL  COMMENT 'TSA 공인 시각 (UTC)',
    hash_algorithm  VARCHAR(20)  NOT NULL  DEFAULT 'SHA-256',
    message_imprint VARBINARY(64) NOT NULL COMMENT 'TSA가 타임스탬프한 콘텐츠 해시',
    token_der       LONGBLOB     NOT NULL  COMMENT 'DER 인코딩된 RFC 3161 TimeStampToken',
    created_at      DATETIME(6)  NOT NULL  DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_ts_token_signature (signature_id),
    CONSTRAINT fk_ts_token_signature
        FOREIGN KEY (signature_id) REFERENCES signatures(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RFC 3161 타임스탬프 토큰';
