ALTER TABLE document_bundle_items
    DROP PRIMARY KEY,
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT FIRST,
    ADD PRIMARY KEY (id),
    ADD UNIQUE KEY uq_bundle_document (bundle_id, document_id);
