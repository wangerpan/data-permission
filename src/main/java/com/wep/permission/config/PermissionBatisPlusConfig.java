package com.wep.permission.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据权限验证
 */
@Configuration
public class PermissionBatisPlusConfig {
    @Autowired
    private DataScopeHandler dataScopeHandler;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType

        // 添加自定义的数据权限处理器
        CustomDataPermissionInterceptor dataPermissionInterceptor = new CustomDataPermissionInterceptor();
        dataPermissionInterceptor.setDataPermissionHandler(dataScopeHandler);
        interceptor.addInnerInterceptor(dataPermissionInterceptor);
        return interceptor;
    }
}
