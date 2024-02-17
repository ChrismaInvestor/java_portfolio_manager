package com.portfolio.manager.service;

import java.io.IOException;

public interface PriceService {
    Double getLatestPrice(String code) throws IOException;

}
