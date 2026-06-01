package com.qusign

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class QusignApplication

fun main(args: Array<String>) {
	runApplication<QusignApplication>(*args)
}
