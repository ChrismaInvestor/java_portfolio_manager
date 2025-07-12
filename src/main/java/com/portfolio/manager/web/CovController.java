package com.portfolio.manager.web;

import com.portfolio.manager.domain.CovInfo;
import com.portfolio.manager.service.tracking.CovInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequestMapping("/cov")
@RestController
@CrossOrigin
public class CovController {

    @Resource
    CovInfoService covInfoService;

    record Cov(String code, String stock_code, String cov_price, String premium) {
    }

    record ConvertedCov(String code, String stock_code, BigDecimal cov_price, BigDecimal premium) {
    }


    record ReqProp(List<Cov> covs) {
    }

    @PostMapping
    public void addCov(@RequestBody ReqProp prop) {
//        对数据中的标点符号进行处理，并筛选出主板的股票
        var cleanedCovs = prop.covs().stream().map(cov -> {
            var price = cov.cov_price;
            while (price.contains("*")) {
                price = price.replace("*", "");
            }
            String premium = cov.premium.replaceAll("%", "").trim();
            return new ConvertedCov(cov.code, cov.stock_code, new BigDecimal(price), new BigDecimal(premium));
        }).filter(cov -> cov.stock_code.startsWith("6") || cov.stock_code.startsWith("0")).toList();
//        类型转换，只是为了计算溢价率
        var covInfoList = cleanedCovs.stream().map(cov -> {
            CovInfo covInfo = new CovInfo();
            covInfo.setCode(cov.code);
            covInfo.setConvertedPrice(cov.cov_price.doubleValue());
            covInfo.setStockCode(cov.stock_code);
            return covInfo;
        }).toList();
//        计算溢价率
        var covPremiumMap = covInfoService.calPremium(covInfoList);
        List<CovInfo> toSave = new ArrayList<>();
//        将上传数据中，能够计算溢价率的信息，且溢价率差值误差大于1
        cleanedCovs.forEach(cov -> {
            if(!covPremiumMap.containsKey(cov.code)){
                log.info("missing code: {}", cov);
                return;
            }
            if (Math.abs(covPremiumMap.get(cov.code).subtract(cov.premium).doubleValue()) > 1) {
                log.info("premium diff: {}, premium offical: {}", covPremiumMap.get(cov.code).subtract(cov.premium), cov.premium);
            } else {
                CovInfo covInfo = new CovInfo();
                covInfo.setConvertedPrice(cov.cov_price.doubleValue());
                covInfo.setCode(cov.code);
                covInfo.setStockCode(cov.stock_code);
                toSave.add(covInfo);
            }
        });
        covInfoService.saveAll(toSave);
    }
}
