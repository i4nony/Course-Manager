package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.IPadDao;
import com.wumii.wechat.entity.IPad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Profiling
@Service
public class IPadService {
    @Autowired
    private IPadDao iPadDao;
    List<IPad> getUsedIPad() {
        return iPadDao.findByOccupiedIsTrue();
    }
}
