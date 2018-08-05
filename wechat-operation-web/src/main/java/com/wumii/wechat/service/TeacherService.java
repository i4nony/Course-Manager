package com.wumii.wechat.service;

import com.wumii.application.exception.ShowOnlyErrorMobileResultException;
import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.TeacherDao;
import com.wumii.wechat.entity.Teacher;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.entity.base.TeacherStatus;
import com.wumii.wechat.result.TeacherInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Profiling
@Service
public class TeacherService {
    private static final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    @Autowired
    private WeChatUserService weChatUserService;
    @Autowired
    private TeacherDao teacherDao;
    @Autowired
    private IPadService iPadService;
    @Autowired
    private WeChatLoginService weChatLoginService;

    public List<TeacherInfo> getTeachers() {
        List<Teacher> teachers = teacherDao.findByDeletedIsFalseOrderByCreationTimeDesc();
        List<TeacherInfo> teacherInfos = new ArrayList<>();
        List<String> loginTeacherWxids = weChatLoginService.loginTeacherWxid();
        for(Teacher teacher : teachers) {
            WeChatUser weChatUser = weChatUserService.getOne(teacher.getWxid());
            // TODO: 2018/7/2  返回老师的二维码
            TeacherStatus status = loginTeacherWxids.contains(teacher.getWxid()) ? TeacherStatus.STARTED : TeacherStatus.STOPPED;
            TeacherInfo teacherInfo = new TeacherInfo(weChatUser.getWxid(), weChatUser.getNickName(),
                    "", status);
            teacherInfos.add(teacherInfo);
        }
        return teacherInfos;
    }

    @Transactional
    public void setDeleted(String wxId) {
        Teacher teacher = teacherDao.findOne(wxId);
        teacher.setDeleted(true);
        teacherDao.save(teacher);
        weChatLoginService.logout();
    }

    public boolean uploadPersonalQrcode(String wxId, MultipartFile qrCodeFile) {
        Teacher teacher = teacherDao.findOne(wxId);
        if (teacher == null || teacher.isDeleted()) {
            throw new ShowOnlyErrorMobileResultException("老师不存在或已被删除");
        }
        // TODO: 2018/7/4 调用上传图片的接口
        teacher.setQrcodeUrlPath("");
        teacherDao.save(teacher);
        return true;
    }
}
