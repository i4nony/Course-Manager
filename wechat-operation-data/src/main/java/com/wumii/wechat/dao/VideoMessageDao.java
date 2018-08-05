package com.wumii.wechat.dao;

import com.wumii.wechat.entity.VideoMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoMessageDao extends JpaRepository<VideoMessage, Long> {
}
