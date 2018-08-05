package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.wechat.result.MassMessageInfo;
import com.wumii.wechat.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public class MessageController extends ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/message/group/statistics", method = RequestMethod.GET)
    public WebResult getGroupMessageStatistics(@RequestParam("wxId") String wxId) {
        return WebResult.success(messageService.getGroupMessageStatistics(wxId));
    }

    @RequestMapping(value = "/message/history/group", method = RequestMethod.GET)
    public WebResult getGroupMessages(@RequestParam("wxId") String wxId, @RequestParam(value = "maxId", required = false, defaultValue = "0") long maxId,
                          @RequestParam(value = "size") int size) {
        if (maxId <= 0) {
            maxId = Long.MAX_VALUE;
        }
        if (size <= 0) {
            size = 10;
        }
        return WebResult.success(messageService.getGroupMessages(wxId, maxId, size));
    }

    @RequestMapping(value = "/message/history/user", method = RequestMethod.GET)
    public WebResult getUserMessages(@RequestParam("userWxid") String userWxid, @RequestParam("teacherWxid") String teacherWxid,
                              @RequestParam(value = "maxId", required = false, defaultValue = "0") long maxId, @RequestParam("size") int size) {
        if (maxId <= 0) {
            maxId = Long.MAX_VALUE;
        }
        if (size <= 0) {
            size = 10;
        }
        return WebResult.success(messageService.getUserMessages(userWxid, teacherWxid, maxId, size));
    }

    @RequestMapping(value = "/message/send", method = RequestMethod.POST)
    public WebResult sendMessage(@RequestBody MassMessageInfo massMessageInfo) throws IOException, InterruptedException {
        messageService.sendMessage(massMessageInfo);
        return WebResult.success();
    }
}
