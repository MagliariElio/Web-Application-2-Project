    package it.polito.students.clientouth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ClientOAuthApplication

fun main(args: Array<String>) {
    runApplication<ClientOAuthApplication>(*args)
}
