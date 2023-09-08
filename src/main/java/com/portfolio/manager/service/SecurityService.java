package com.portfolio.manager.service;

import com.portfolio.manager.domain.Security;

public interface SecurityService {
    String getSecurityName(String securityCode);

    void addSecurity(Security security);


}
