package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.wechat.service.WeChatGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class WeChatGroupController extends ErrorController {
    @Autowired
    private WeChatGroupService weChatGroupService;

    @RequestMapping(value = "/group/list", method = RequestMethod.GET)
    public WebResult getGroupList() {
        return WebResult.success(weChatGroupService.getAllGroups());
    }

    @RequestMapping(value = "/group/delete", method = RequestMethod.POST)
    public WebResult deleteGroup(@RequestParam("wxid") String wxid) {
        if (weChatGroupService.delete(wxid)) {
            return WebResult.success();
        }
        return WebResult.error("删除失败");
    }

    @RequestMapping(value = "/group/create", method = RequestMethod.POST)
    public WebResult createGroup(
            @RequestParam("teacherWxid") String teacherWxid,
            @RequestParam("wxids") List<String> wxids) throws InterruptedException {
        weChatGroupService.createGroup(wxids, teacherWxid);
        return WebResult.success();
    }

    @RequestMapping(value = "/group/invite", method = RequestMethod.POST)
    public WebResult Invite(@RequestParam("teacherWxid") String teacherWxid, @RequestParam("wxids") List<String> wxids) throws InterruptedException {
        weChatGroupService.inviteToGroup(wxids, teacherWxid);
        return WebResult.success();
    }
}
