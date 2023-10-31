package com.vctek.orderservice.elasticsearch.service.impl;

import com.vctek.service.impl.DefaultFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("orderFileService")
public class OrderFileService extends DefaultFileService {

    @Value("${vctek.download.orderList:/opt/download/orders}")
    @Override
    public void setRootDownloadPath(String rootDownloadPath) {
        this.rootDownloadPath = rootDownloadPath;
    }
}
