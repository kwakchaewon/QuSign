CREATE TABLE notifications
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    type         VARCHAR(30)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    message      VARCHAR(500) NOT NULL,
    reference_id BIGINT       NULL,
    is_read      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notifications_user_id    ON notifications (user_id);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, is_read);
