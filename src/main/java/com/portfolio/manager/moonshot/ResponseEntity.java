package com.portfolio.manager.moonshot;

import java.util.List;

public record ResponseEntity(String object, List<Data> data) {
}
