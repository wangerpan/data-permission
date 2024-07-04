package com.wep.permission.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.wep.permission.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充数据
 */
@Slf4j
@Component
public class UacMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createUser", String.class, TokenUtils.getCurrentUserId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", String.class, TokenUtils.getCurrentUserId());
        this.setFieldValByName("updateUser", TokenUtils.getCurrentUserId(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
