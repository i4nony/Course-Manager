package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.wechat.service.SentMassMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentMassMessageController extends ErrorController {
    @Autowired
    private SentMassMessageService sentMassMessageService;

    @RequestMapping(value = "/mass-message-history/list", method = RequestMethod.GET)
    public WebResult getSentMassMessageList() {
        return WebResult.success(sentMassMessageService.getAllMassMessageHistory());
    }
}
