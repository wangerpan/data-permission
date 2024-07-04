package com.wep.permission.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.wep.permission.annotation.DataScope;
import com.wep.permission.annotation.DataScopeExpression;
import com.wep.permission.dto.DataScopeInfo;
import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.enums.ProvideTypeEnum;
import com.wep.permission.exception.PermissionException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wep
 */
@Aspect
@Slf4j
@Component
public class DataScopeAspect {


    /**
     * 通过ThreadLocal记录权限相关的属性值
     */
    private static ThreadLocal<DataScopeInfo> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<Boolean> methodProcessed = new ThreadLocal<>();

    @Resource
    private ApplicationContext context;

    public static DataScopeInfo getDataScopeInfo() {
        return threadLocal.get();
    }

    // 方法切点
    @Pointcut("@annotation(com.wep.permission.annotation.DataScope)")
    public void dataScopePointCut() {
    }

    @After("dataScopePointCut()")
    public void clearThreadLocal() {
        threadLocal.remove();
        methodProcessed.remove();
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) {
        if (methodProcessed.get() != null && methodProcessed.get()) {
            return;
        } else {
            methodProcessed.set(true);
        }
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        // 获得作用于方法上的注解
        DataScope dataScope = method.getAnnotation(DataScope.class);
        // 如果接口上也获取不到,则添加的该注解无效 或者把注解成非必须
        if (dataScope == null || !dataScope.required()) {
            return;
        }
        try {
            // 数据对象范围控制
            DataScopeInfo dataScopeInfo = new DataScopeInfo();
            DataScopeExpression[] dataScopeExpressions = dataScope.dataScopeExpressions();
            if (ArrayUtil.isNotEmpty(dataScopeExpressions)) {
                List<DataScopeRuleDTO> ruleList = Arrays.stream(dataScopeExpressions).map(this::getDataScopeRuleDTO
                ).collect(Collectors.toList());
                dataScopeInfo.setRuleList(ruleList);
            }
            threadLocal.set(dataScopeInfo);
        } catch (Exception e) {
            throw new PermissionException("数据对象注解切面逻辑报错：" + e.getMessage());
        }
    }

    private DataScopeRuleDTO getDataScopeRuleDTO(DataScopeExpression dataScopeExpression) {
        DataScopeRuleDTO dataScopeRuleDTO = new DataScopeRuleDTO();

        dataScopeRuleDTO.setSpliceType(dataScopeExpression.spliceType().name());
        dataScopeRuleDTO.setTableAlias(dataScopeExpression.tableAlias());
        dataScopeRuleDTO.setColumnName(dataScopeExpression.columnName());
        dataScopeRuleDTO.setExpression(dataScopeExpression.expression().name());
        dataScopeRuleDTO.setProvideType(dataScopeExpression.provideTypeEnum().getCode());

        // 处理值类型为method
        handleMethodType(dataScopeExpression, dataScopeRuleDTO);

        dataScopeRuleDTO.setValue(dataScopeExpression.value());
        dataScopeRuleDTO.setFormalParam(dataScopeExpression.formalParam());
        dataScopeRuleDTO.setActualParam(dataScopeExpression.actualParam());
        return dataScopeRuleDTO;
    }

    /**
     * 处理值类型为方法的逻辑
     *
     * @param dataScopeExpression
     * @param dataScopeRuleDTO
     */
    private void handleMethodType(DataScopeExpression dataScopeExpression, DataScopeRuleDTO dataScopeRuleDTO) {
        if (!ProvideTypeEnum.METHOD.getCode().equals(dataScopeExpression.provideTypeEnum().getCode())) {
            return;
        }

        try {
            Class<?>[] paramsTypes = null;
            Object[] argValues = null;

            if (StrUtil.isNotBlank(dataScopeExpression.formalParam()) && StrUtil.isNotBlank(dataScopeExpression.actualParam())) {
                // 获取形参数组
                String[] formalArray = dataScopeExpression.formalParam().split(";");
                // 获取实参数组
                String[] actualArray = dataScopeExpression.actualParam().split(";");

                if (formalArray.length != actualArray.length)
                    throw new RuntimeException("形参数量与实参数量不符合");

                // 转换形参为Class数组
                paramsTypes = new Class<?>[formalArray.length];
                for (int i = 0; i < formalArray.length; i++) {
                    paramsTypes[i] = Class.forName(formalArray[i].trim());
                }

                // 转换实参为Object数组
                argValues = new Object[actualArray.length];
                for (int i = 0; i < actualArray.length; i++) {
                    argValues[i] = JSONObject.parseObject(actualArray[i], paramsTypes[i]);
                }
            }
            String[] parts = dataScopeExpression.value().split("#");
            String className = parts[0];
            String methodName = parts[1];

            Class<?> clazz = Class.forName(className);
            Object result;

            Method targetMethod = clazz.getDeclaredMethod(methodName, paramsTypes);
            if (Modifier.isStatic(targetMethod.getModifiers())) {
                // 设置静态方法可访问
                targetMethod.setAccessible(true);
                // 执行静态方法
                result = targetMethod.invoke(null, argValues);
            } else {
                // 尝试从容器中获取实例
                Object instance = context.getBean(Class.forName(className));
                Class<?> beanClazz = instance.getClass();
                Method beanClazzMethod = beanClazz.getDeclaredMethod(methodName, paramsTypes);
                // 执行方法
                result = beanClazzMethod.invoke(instance, argValues);
            }
            dataScopeRuleDTO.setResult(result);
        } catch (NoSuchBeanDefinitionException e) {
            throw new PermissionException("没有相关的bean");
        } catch (NoSuchMethodException e) {
            throw new PermissionException("配置了不存在的方法");
        } catch (ClassNotFoundException e) {
            throw new PermissionException("配置了不存在的类");
        } catch (Exception e) {
            throw new PermissionException("其他错误：" + e.getMessage());
        }
    }
}
