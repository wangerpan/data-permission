package com.wep.permission.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 自动装配切面和工具类。
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan("com.wep.permission")
public class DataPermissionAutoConfiguration {
}
