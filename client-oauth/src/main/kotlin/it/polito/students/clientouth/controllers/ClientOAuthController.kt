package it.polito.students.clientouth.controllers

import it.polito.students.clientouth.dtos.CredentialsDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/API/login")
class ClientOAuthController {

    @PostMapping("", "/")
    fun login(
        @RequestBody credentials: CredentialsDTO
    ): ResponseEntity<out Any> {
        try{

            return ResponseEntity.ok().body("userLogged")
        }catch (error: Exception){
            throw Exception()
        }
    }

}