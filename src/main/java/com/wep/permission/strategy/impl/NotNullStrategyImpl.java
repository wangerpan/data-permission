package com.wep.permission.strategy.impl;

import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.strategy.ExpressStrategy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import org.springframework.stereotype.Service;

/**
 * @author wep  2024/6/26
 */
@Service("NOT_NULL")
public class NotNullStrategyImpl implements ExpressStrategy {

    @Override
    public Expression apply(DataScopeRuleDTO rule, Expression where) {
        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setNot(true);
        isNullExpression.setLeftExpression(getColumn(rule));
        return assemble(rule.getSpliceType(), where, isNullExpression);
    }
}
