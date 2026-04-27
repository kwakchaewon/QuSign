CREATE TABLE users
(
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    email                 VARCHAR(255) NOT NULL,
    password              VARCHAR(255) NOT NULL,
    encrypted_private_key TEXT         NOT NULL,
    public_key            TEXT         NOT NULL,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE documents
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key       VARCHAR(500) NOT NULL,
    hash_sha3_256     VARCHAR(64)  NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_documents_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE signature_requests
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    document_id  BIGINT       NOT NULL,
    requester_id BIGINT       NOT NULL,
    signer_email VARCHAR(255) NOT NULL,
    token        VARCHAR(36)  NOT NULL,
    expires_at   DATETIME     NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_signature_requests_token (token),
    CONSTRAINT fk_sig_req_document  FOREIGN KEY (document_id)  REFERENCES documents (id),
    CONSTRAINT fk_sig_req_requester FOREIGN KEY (requester_id) REFERENCES users (id)
);

CREATE TABLE signatures
(
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    signature_request_id BIGINT       NOT NULL,
    signer_id            BIGINT       NOT NULL,
    signed_storage_key   VARCHAR(500) NOT NULL,
    signature_value      TEXT         NOT NULL,
    signed_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_signatures_request FOREIGN KEY (signature_request_id) REFERENCES signature_requests (id),
    CONSTRAINT fk_signatures_signer  FOREIGN KEY (signer_id)            REFERENCES users (id)
);
