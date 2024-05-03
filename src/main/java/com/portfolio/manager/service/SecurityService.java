package com.portfolio.manager.service;

import com.portfolio.manager.domain.Security;

import java.util.List;

public interface SecurityService {
    String getSecurityName(String securityCode);

    void addSecurity(Security security);

    List<Security> listExistingStocks();

    void clearData();
}
