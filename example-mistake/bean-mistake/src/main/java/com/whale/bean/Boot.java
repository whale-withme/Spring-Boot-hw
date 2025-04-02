package com.whale.bean;

import org.springframework.boot.CommandLineRunner;

import com.whale.bean.service.ServiceImpl;

public class Boot implements CommandLineRunner{
    public void run(String... args) throws Exception{
        ServiceImpl serviceImpl = new ServiceImpl("args");
    }
}
