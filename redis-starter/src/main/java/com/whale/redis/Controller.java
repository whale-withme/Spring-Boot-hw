package com.whale.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whale.redis.dao.RedisDaoImpl;

@RestController
@RequestMapping("/rest")
public class Controller {
    
    @Autowired
    private RedisDaoImpl redisDaoImpl;


    @PostMapping("/set")
    public String RestSetuser(@RequestBody User user){
        return redisDaoImpl.addUser(user);
    }

    @RequestMapping("/get/{id}")
    public User RestGetuserById(@PathVariable String id){
        return redisDaoImpl.getUserById(id);
    }
}
