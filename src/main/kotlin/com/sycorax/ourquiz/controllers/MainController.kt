package com.sycorax.ourquiz

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
@RestController
class MainController {
    @GetMapping("/hello")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): String {
        return "Hello " + name;
    }
}