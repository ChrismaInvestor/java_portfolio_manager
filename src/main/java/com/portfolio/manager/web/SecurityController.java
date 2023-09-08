package com.portfolio.manager.web;

import com.portfolio.manager.domain.Security;
import com.portfolio.manager.service.SecurityService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("security")
@RestController
public class SecurityController {
    @Resource
    private SecurityService securityService;

    @PostMapping
    public void addSecurity(@RequestBody Security security) {
        securityService.addSecurity(security);
    }
}
