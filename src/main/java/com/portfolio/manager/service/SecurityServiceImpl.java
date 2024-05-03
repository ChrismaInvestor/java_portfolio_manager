package com.portfolio.manager.service;

import com.portfolio.manager.domain.Security;
import com.portfolio.manager.repository.CbStockMappingRepo;
import com.portfolio.manager.repository.SecurityRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SecurityServiceImpl implements SecurityService {
    @Resource
    private SecurityRepo securityRepo;

    @Resource
    private CbStockMappingRepo cbStockMappingRepo;

    @Override
    public String getSecurityName(String securityCode) {
        Security security = securityRepo.findOneByCode(securityCode);
        return security == null ? null : security.getName();
    }

    @Override
    public void addSecurity(Security security) {
        if (security.getName().matches(".*" + "[\\u4e00-\\u9fa5]" + ".*")) {
            log.info("security: {}", security);
            securityRepo.save(security);
        }
    }

    @Override
    public List<Security> listExistingStocks() {
        return securityRepo.findAll();
    }

    @Override
    public void clearData() {
        securityRepo.deleteAll();
        cbStockMappingRepo.deleteAll();
    }
}
