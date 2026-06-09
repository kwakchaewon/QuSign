CREATE TABLE audit_logs (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    event_type           VARCHAR(50)  NOT NULL,
    actor_email          VARCHAR(255) NOT NULL,
    signature_request_id BIGINT,
    bundle_id            BIGINT,
    document_id          BIGINT,
    ip_address           VARCHAR(45)  NOT NULL,
    user_agent           VARCHAR(500) NOT NULL,
    created_at           DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_audit_document_id    ON audit_logs (document_id);
CREATE INDEX idx_audit_bundle_id      ON audit_logs (bundle_id);
CREATE INDEX idx_audit_actor_email    ON audit_logs (actor_email);
CREATE INDEX idx_audit_created_at     ON audit_logs (created_at);
