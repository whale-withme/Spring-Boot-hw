package com.whale.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rabbit")
public class OrderController {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @GetMapping("/order")
    public String createOrder(){
        return "ok";
    }
}
