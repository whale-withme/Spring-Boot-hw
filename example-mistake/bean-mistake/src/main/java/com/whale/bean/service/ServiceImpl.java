package com.whale.bean.service;

import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor  // 加上无参构造器，spring 默认是用无参构造器
public class ServiceImpl {
    // 注册成功bean需要：寻找到合适的构造器(准确到某一个，确定参数)、通过反射构造实例
    // 这里有构造器，但是参数并没有指定，可能会使用默认参数构造，也可能会出现error
    private String serviceName;

    // 可以使用@Value("${service.name:default}")指定：或者 aotowired 注入，另外再注册一个string返回类型的bean
    public ServiceImpl(String serviceName){
        this.serviceName = serviceName;
        System.out.println(serviceName);
    }
}
