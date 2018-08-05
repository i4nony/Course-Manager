package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Profiling
@Service
public class LoginService {
    @Autowired
    private WeChatLoginService weChatLoginService;
    public byte[] getQrcode() throws IOException{
        return weChatLoginService.login();
    }

    public void logout(String wxid) {
        weChatLoginService.setCurrentWxid(wxid);
        weChatLoginService.logout();
    }
}
