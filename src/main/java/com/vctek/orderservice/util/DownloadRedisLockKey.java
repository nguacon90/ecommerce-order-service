package com.vctek.orderservice.util;

public enum DownloadRedisLockKey {
    DOWNLOAD_ORDER("DOWNLOAD_ORDER_{orderType}_{userId}"),
    USER_ID_PATTEN("{userId}"),
    ORDER_TYPE_PATTEN("{orderType}"),
    ;

    private String key;

    DownloadRedisLockKey(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

}
