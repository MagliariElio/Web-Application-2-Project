package it.polito.students.crm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableWebMvc
class CrmApplication

fun main(args: Array<String>) {
    runApplication<CrmApplication>(*args)
}
