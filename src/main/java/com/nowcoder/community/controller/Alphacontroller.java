package com.nowcoder.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/Alpha")
public class Alphacontroller {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello Spring";
    }

}
