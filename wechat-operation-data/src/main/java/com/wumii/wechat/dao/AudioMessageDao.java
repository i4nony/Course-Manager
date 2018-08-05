package com.wumii.wechat.dao;

import com.wumii.wechat.entity.AudioMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioMessageDao extends JpaRepository<AudioMessage, Long> {
}
