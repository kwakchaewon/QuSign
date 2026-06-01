package com.qusign.notification.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotificationNotFoundException : RuntimeException("알림을 찾을 수 없습니다.")
