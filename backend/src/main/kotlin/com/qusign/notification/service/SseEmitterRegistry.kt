package com.qusign.notification.service

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Component
class SseEmitterRegistry {

    private val emitters = ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>>()

    fun register(userId: Long): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters.getOrPut(userId) { CopyOnWriteArrayList() }.add(emitter)

        val cleanup = Runnable { remove(userId, emitter) }
        emitter.onCompletion(cleanup)
        emitter.onTimeout(cleanup)
        emitter.onError { cleanup.run() }

        // 연결 직후 heartbeat 전송 (브라우저 연결 확인용)
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"))
        } catch (_: Exception) {
            remove(userId, emitter)
        }

        return emitter
    }

    fun send(userId: Long, data: String) {
        emitters[userId]?.removeIf { emitter ->
            try {
                emitter.send(SseEmitter.event().name("notification").data(data))
                false
            } catch (_: Exception) {
                true
            }
        }
    }

    private fun remove(userId: Long, emitter: SseEmitter) {
        emitters[userId]?.remove(emitter)
    }
}
