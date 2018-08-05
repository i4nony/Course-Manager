package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.application.util.JsonUtils;
import com.wumii.wechat.domain.MessagesInfo;
import com.wumii.wechat.service.MassMessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MassMessageTemplateController extends ErrorController {
    @Autowired
    private MassMessageTemplateService massMessageTemplateService;

    @RequestMapping(value = "/mass-message-template/list", method = RequestMethod.GET)
    public WebResult getMassMessageTemplateList() {
        return WebResult.success(massMessageTemplateService.findAllMassMessageTemplate());
    }

    @RequestMapping(value = "/mass-message-template", method = RequestMethod.POST)
    public WebResult addMassMessageTemplate(@RequestBody MessagesInfo messagesInfo) {
        if (massMessageTemplateService.addMassMessageTemplate(JsonUtils.serialize(messagesInfo)) == null) {
            return WebResult.error("添加群发模板失败");
        }
        return WebResult.success();
    }

    @RequestMapping(value = "/mass-message-template/delete", method = RequestMethod.POST)
    public WebResult deleteMassMessageTemplate(@RequestParam("id") long id) {
        massMessageTemplateService.deleteMassMessageTemplate(id);
        return WebResult.success();
    }
}
