package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.application.util.JsonUtils;
import com.wumii.wechat.entity.base.ReplyType;
import com.wumii.wechat.result.AutoReplyTemplyInfo;
import com.wumii.wechat.service.AutoReplyTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutoReplyTemplateController extends ErrorController {
    @Autowired
    private AutoReplyTemplateService autoReplyTemplateService;

    @RequestMapping(value = "/auto-reply-template/list", method = RequestMethod.GET)
    public WebResult getAutoReplyTemplateList() {
        return WebResult.success(autoReplyTemplateService.findAllAutoReplyTemplate());
    }

    @RequestMapping(value = "/auto-reply-template", method = RequestMethod.POST)
    public WebResult addAutoReplyTemplate(@RequestBody AutoReplyTemplyInfo autoReplyTemplyInfo) {
        if (autoReplyTemplateService.addAutoReplyTemplate(autoReplyTemplyInfo.getWxid(),
                autoReplyTemplyInfo.getReplyType(), JsonUtils.serialize(autoReplyTemplyInfo.getMessagesInfo())) == null) {
            return WebResult.error("添加回复模板失败");
        }
        return WebResult.success();
    }

    @RequestMapping(value = "/auto-reply-template/delete", method = RequestMethod.POST)
    public WebResult deleteAutoReplyTemplateList(@RequestParam("id") long id) {
        autoReplyTemplateService.deleteAutoReplyTemplate(id);
        return WebResult.success();
    }
}
