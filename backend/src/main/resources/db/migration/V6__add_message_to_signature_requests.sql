ALTER TABLE signature_requests
    ADD COLUMN message VARCHAR(1000) NULL AFTER signer_email;
