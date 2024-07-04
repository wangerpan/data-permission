package com.wep.permission.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.enums.ProvideTypeEnum;
import com.wep.permission.exception.PermissionException;
import com.wep.permission.strategy.ExpressStrategy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wep  2024/6/26
 */
@Service("NOT_IN")
public class NotInStrategyImpl implements ExpressStrategy {

    @Override
    public Expression apply(DataScopeRuleDTO rule, Expression where) {
        Object value = getValue(rule);
        if (rule.getProvideType().equals(ProvideTypeEnum.VALUE.getCode())) {
            String[] split = ((String) value).split(",");
            value = Arrays.asList(split);
        }

        if (!(value instanceof List<?>)) {
            throw new PermissionException("表达式为IN 或 NOT IN 时，反射执行方法的返回值必须是集合类型");
        }

        if (CollectionUtil.isEmpty((List<?>) value)) {
            return where;
        }
        ExpressionList itemsList = new ExpressionList(((List<?>) value).stream().map(v -> new StringValue(String.valueOf(v))).collect(Collectors.toList()));
        InExpression inExpression = new InExpression(getColumn(rule), itemsList);
        NotExpression notExpression = new NotExpression(inExpression);
        return assemble(rule.getSpliceType(), where, notExpression);
    }
}
