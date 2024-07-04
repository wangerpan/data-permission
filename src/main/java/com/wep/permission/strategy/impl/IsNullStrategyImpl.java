package com.wep.permission.strategy.impl;

import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.strategy.ExpressStrategy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import org.springframework.stereotype.Service;

/**
 * @author wep  2024/6/26
 */
@Service("IS_NULL")
public class IsNullStrategyImpl implements ExpressStrategy {

    @Override
    public Expression apply(DataScopeRuleDTO rule, Expression where) {
        Column column = getColumn(rule);
        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(column);
        return assemble(rule.getSpliceType(), where, isNullExpression);
    }
}
