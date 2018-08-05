package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.wechat.entity.base.TeacherStatus;
import com.wumii.wechat.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;

@RestController
public class TeacherController extends ErrorController {
    @Autowired
    private TeacherService teacherService;

    @RequestMapping(value = "/teacher/list", method = RequestMethod.GET)
    public WebResult getTeacherList() {
        return WebResult.success(teacherService.getTeachers());
    }

    @RequestMapping(value = "/teacher/delete", method = RequestMethod.POST)
    public WebResult deleteTeacher(@RequestParam("wxId") String wxId) {
        teacherService.setDeleted(wxId);
        return WebResult.success();
    }

    @RequestMapping(value = "/teacher/personal-qrcode", method = RequestMethod.POST)
    public WebResult uploadPersonalQrcode(@RequestParam("wxId") String wxId,
                                          @RequestParam("qrCodeFile") MultipartFile qrCodeFile) {
        if (teacherService.uploadPersonalQrcode(wxId, qrCodeFile)) {
            return WebResult.success();
        }
        return WebResult.error("上传二维码失败");
    }
}
