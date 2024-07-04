package com.wep.permission.strategy.impl;

import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.strategy.ExpressStrategy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import org.springframework.stereotype.Service;

/**
 * @author wep  2024/6/26
 */
@Service("GE")
public class GeStrategyImpl implements ExpressStrategy {

    @Override
    public Expression apply(DataScopeRuleDTO rule, Expression where) {
        StringValue valueExpression = new StringValue(String.valueOf(getValue(rule)));
        GreaterThanEquals greaterThanEquals = new GreaterThanEquals();
        greaterThanEquals.setLeftExpression(getColumn(rule));
        greaterThanEquals.setRightExpression(valueExpression);
        return assemble(rule.getSpliceType(), where, greaterThanEquals);
    }
}
