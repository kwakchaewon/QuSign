package com.qusign.audit

import java.time.Period

/**
 * 전자서명법 제31조: 전자서명 관련 기록 10년 보존 의무.
 * retained_until 은 레코드 생성 시점에 한 번 기록하며 이후 변경·삭제 금지.
 */
object RetentionPolicy {
    val AUDIT_LOG: Period = Period.ofYears(10)
}
