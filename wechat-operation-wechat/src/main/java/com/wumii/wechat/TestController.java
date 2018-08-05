package com.wumii.wechat;

import com.wumii.application.controller.ErrorController;
import com.wumii.wechat.service.WeChatChatRoomService;
import com.wumii.wechat.service.WeChatLoginService;
import com.wumii.wechat.service.WeChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

//测试用
@Controller
@RequestMapping("/test")
public class TestController extends ErrorController {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final WeChatLoginService weChatLoginService;
    private final WeChatChatRoomService weChatChatRoomService;
    private final WeChatMessageService weChatMessageService;

    @Autowired
    public TestController(HttpServletRequest request,
                          HttpServletResponse response,
                          WeChatLoginService weChatLoginService,
                          WeChatChatRoomService weChatChatRoomService,
                          WeChatMessageService weChatMessageService) {
        this.request = request;
        this.response = response;
        this.weChatLoginService = weChatLoginService;
        this.weChatChatRoomService = weChatChatRoomService;
        this.weChatMessageService = weChatMessageService;
    }

    @GetMapping("/login")
    public ModelAndView login() throws IOException {
        byte[] qrCode = weChatLoginService.login();
        response.setContentType("image/jpeg");
        response.getOutputStream().write(qrCode);
        response.getOutputStream().flush();
        return null;
    }

    @GetMapping("/logout")
    @ResponseBody
    public String logout() {
        weChatLoginService.logout();
        return "success";
    }

    @GetMapping("/group")
    @ResponseBody
    public String createGroup(@RequestParam("wxids") List<String> wxids) throws InterruptedException {
        weChatChatRoomService.createChatRoom(wxids);
        return "success";
    }

    @GetMapping("/message")
    @ResponseBody
    public String sendMessage(@RequestParam("wxids") List<String> wxids,
                              @RequestParam("content") String content) throws InterruptedException {
        weChatMessageService.sendTextMessage(wxids, content, null);
        return "success";
    }

}
