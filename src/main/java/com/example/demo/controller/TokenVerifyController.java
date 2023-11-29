package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "https://www.mimamaori.tech")
@RestController
@RequestMapping("/")
public class TokenVerifyController {
	 @GetMapping("/verify")
	  public String verifyToken(
			  @RequestParam(required = true) String token) {
		 
		 return "OK";
	  }
}
