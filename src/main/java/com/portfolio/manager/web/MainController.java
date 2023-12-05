package com.portfolio.manager.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("trade")
public class MainController {
    @GetMapping
    public ModelAndView frontPage(){
        return new ModelAndView("index");
    }
}
