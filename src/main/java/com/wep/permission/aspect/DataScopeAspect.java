package com.wep.permission.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.wep.permission.annotation.DataScope;
import com.wep.permission.annotation.DataScopeExpression;
import com.wep.permission.context.DataScopeContext;
import com.wep.permission.enums.ExpressionEnum;
import com.wep.permission.enums.SpliceTypeEnum;
import com.wep.permission.utils.TokenUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据范围切面：在执行方法前计算 SQL 片段，执行后清理上下文。
 */
@Aspect
@Component
public class DataScopeAspect {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Before("@annotation(dataScope)")
    public void before(JoinPoint joinPoint, DataScope dataScope) {
        String condition = buildCondition(dataScope);
        DataScopeContext.setScopeCondition(condition);
    }

    @After("@annotation(com.wep.permission.annotation.DataScope)")
    public void after() {
        DataScopeContext.clear();
    }

    private String buildCondition(DataScope dataScope) {
        List<String> segments = new ArrayList<>();
        for (DataScopeExpression expr : dataScope.dataScopeExpressions()) {
            String value = resolveValue(expr.value());
            String formattedValue = formatValue(expr.expression(), value);
            String segment = expr.tableAlias() + "." + expr.columnName() + " " + expr.expression().getSymbol() + " " + formattedValue;
            segments.add(segment + (expr.spliceType() == SpliceTypeEnum.OR ? " OR " : " AND "));
        }
        if (CollUtil.isEmpty(segments)) {
            return StrUtil.EMPTY;
        }
        // 去除最后多余的 AND/OR
        String joined = segments.stream().map(this::trimTail).collect(Collectors.joining());
        return "(" + joined + ")";
    }

    private String resolveValue(String rawValue) {
        if (StrUtil.startWith(rawValue, "com.wep.permission.utils.TokenUtils#")) {
            String method = StrUtil.removePrefix(rawValue, "com.wep.permission.utils.TokenUtils#");
            if (StrUtil.equals(method, "getCurrentUserId")) {
                return TokenUtils.getCurrentUserId();
            }
        }
        if (StrUtil.startWith(rawValue, "#{")) {
            return PARSER.parseExpression(rawValue.substring(2, rawValue.length() - 1)).getValue(String.class);
        }
        return rawValue;
    }

    private String formatValue(ExpressionEnum expression, String value) {
        if (expression == ExpressionEnum.IN) {
            return "(" + value + ")";
        }
        if (expression == ExpressionEnum.LIKE) {
            return "'%" + value + "%'";
        }
        return "'" + value + "'";
    }

    private String trimTail(String segment) {
        if (segment.endsWith(" AND ")) {
            return segment.substring(0, segment.length() - 5);
        }
        if (segment.endsWith(" OR ")) {
            return segment.substring(0, segment.length() - 4);
        }
        return segment;
    }
}
