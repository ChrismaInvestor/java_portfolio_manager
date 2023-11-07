package com.portfolio.manager.service;

import com.portfolio.manager.domain.Security;
import com.portfolio.manager.repository.SecurityRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SecurityServiceImpl implements SecurityService {
    @Resource
    private SecurityRepo securityRepo;


    @Override
    public String getSecurityName(String securityCode) {
        Security security = securityRepo.findOneByCode(securityCode);
        return security == null ? null : security.getName();
    }

    @Override
    public void addSecurity(Security security) {
        Security existedSecurity = securityRepo.findOneByCode(security.getCode());
        if (existedSecurity == null) {
            if (security.getName().matches(".*" + "[\\u4e00-\\u9fa5]"+ ".*")){
                securityRepo.save(security);
            }
        } else {
            existedSecurity.setName(security.getName());
            securityRepo.save(existedSecurity);
        }
    }

    @Override
    public List<Security> listExistingStocks() {
        return securityRepo.findAll();
    }

}
