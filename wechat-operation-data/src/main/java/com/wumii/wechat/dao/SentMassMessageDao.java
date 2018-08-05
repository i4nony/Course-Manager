package com.wumii.wechat.dao;

import com.wumii.wechat.entity.SentMassMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentMassMessageDao extends JpaRepository<SentMassMessage, Long> {
    List<SentMassMessage> findAllByOrderByIdDesc();
}
