CREATE TABLE document_bundles
(
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    owner_id   BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_bundles_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE document_bundle_items
(
    bundle_id   BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    ordinal     INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (bundle_id, document_id),
    CONSTRAINT fk_bundle_items_bundle   FOREIGN KEY (bundle_id)   REFERENCES document_bundles (id),
    CONSTRAINT fk_bundle_items_document FOREIGN KEY (document_id) REFERENCES documents (id)
);

ALTER TABLE signature_requests
    ADD COLUMN bundle_id    BIGINT      NULL,
    ADD COLUMN bundle_token VARCHAR(36) NULL,
    ADD CONSTRAINT fk_sig_req_bundle FOREIGN KEY (bundle_id) REFERENCES document_bundles (id),
    ADD INDEX idx_sig_req_bundle_token (bundle_token);
