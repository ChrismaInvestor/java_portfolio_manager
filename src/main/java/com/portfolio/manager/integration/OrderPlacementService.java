package com.portfolio.manager.integration;

public interface OrderPlacementService {

    // 返回orderId
    String buy(String code, Double price, Integer vol);

    // 返回orderId
    String sell(String code, Double price, Integer vol);

}
