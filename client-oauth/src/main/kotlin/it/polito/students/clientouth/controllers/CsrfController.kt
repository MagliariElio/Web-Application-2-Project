package it.polito.students.clientouth.controllers

import org.springframework.http.ResponseEntity
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/API/csrf")
class CsrfController {

    @GetMapping("", "/")
    fun csrfToken(@RequestBody csrfToken: CsrfToken): ResponseEntity<String> {
        try{

            return ResponseEntity.ok().body("csrfToken")
        }catch (error: Exception){
            throw Exception()
        }
    }

}