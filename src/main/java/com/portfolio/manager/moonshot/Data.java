package com.portfolio.manager.moonshot;

import java.util.List;

public record Data(Long created, String id, String object, String owned_by, List permission, String root, String parent) {
}
