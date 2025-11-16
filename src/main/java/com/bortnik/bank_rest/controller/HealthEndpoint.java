package com.bortnik.bank_rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthEndpoint {

    @GetMapping("/health" )
    public HttpStatus health() {
        return HttpStatus.OK;
    }
}
