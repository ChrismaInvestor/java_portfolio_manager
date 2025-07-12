package com.portfolio.manager.service.tracking;

import com.portfolio.manager.domain.CovInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CovInfoService {
    void saveAll(List<CovInfo> covInfoList);

    List<CovInfo> tradeableCovs();

    Map<String, BigDecimal> calPremium(List<CovInfo> covInfoList);
}
