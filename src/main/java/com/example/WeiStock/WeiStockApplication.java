package com.example.WeiStock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
@EnableScheduling
public class WeiStockApplication {

public static void main(String[] args) {
	SpringApplication.run(WeiStockApplication.class, args);
}

@GetMapping("/hello")
public String hello(@RequestParam(value = "name", defaultValue = "asdfasdfasdfszdfsadf") String name) {
	return String.format("Hello %s!", name);
}

}