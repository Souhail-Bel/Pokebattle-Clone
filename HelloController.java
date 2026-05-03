package com.example.demo;

import org.springframework.web.bind.annotation.*;

public class HelloController{
    
    @GetMapping("/hello");
    public String hello(){
	return "ahlen\n";
    }
}
