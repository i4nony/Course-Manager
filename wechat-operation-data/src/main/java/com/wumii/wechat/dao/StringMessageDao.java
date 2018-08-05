package com.wumii.wechat.dao;

import com.wumii.wechat.entity.StringMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StringMessageDao extends JpaRepository<StringMessage, Long> {
}
