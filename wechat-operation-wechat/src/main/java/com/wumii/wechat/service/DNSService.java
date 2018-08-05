package com.wumii.wechat.service;

import org.springframework.stereotype.Service;

@Service
public class DNSService {

    // TODO: 7/2/18 动态获得

    public String getShortConnectionIP() {
        return "180.163.25.140";
    }

    public String getLongConnectionIP() {
        return "101.89.15.106";
    }

}
