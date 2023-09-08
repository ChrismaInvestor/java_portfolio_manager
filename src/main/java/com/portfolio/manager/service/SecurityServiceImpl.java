package com.portfolio.manager.service;

import com.portfolio.manager.domain.Security;
import com.portfolio.manager.repository.SecurityRepo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
            securityRepo.save(security);
        } else {
            existedSecurity.setName(security.getName());
            securityRepo.save(existedSecurity);
        }
    }

}
