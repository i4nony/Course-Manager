package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.wechat.service.FriendService;
import com.wumii.wechat.service.WeChatLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FriendController extends ErrorController {

    @Autowired
    private FriendService friendService;


    @RequestMapping(value = "/friend/list", method = RequestMethod.GET)
    public WebResult getFriends(@RequestParam("wxId") String wxId) {
        return WebResult.success(friendService.getFriendInfos(wxId));
    }

    @RequestMapping(value = "/friend/change-remark", method = RequestMethod.POST)
    public WebResult changeRemark(@RequestParam("teacherWxid") String teacherWxid, @RequestParam("friendWxid") String friendWxid,
                            @RequestParam("name") String name) throws InterruptedException {
        friendService.changeRemark(teacherWxid, friendWxid, name);
        return WebResult.success();
    }

    @RequestMapping(value = "/friend/wxid", method = RequestMethod.GET)
    public WebResult getWxid(@RequestParam("nickName") String nickName, @RequestParam("avatarUrl") String avatarUrl) {
        return WebResult.success(friendService.getWxid(nickName, avatarUrl));
    }
}
