package com.wep.permission.strategy.impl;

import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.strategy.ExpressStrategy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.schema.Column;
import org.springframework.stereotype.Service;

/**
 * @author wep  2024/6/26
 */
@Service("GT")
public class GtStrategyImpl implements ExpressStrategy {

    @Override
    public Expression apply(DataScopeRuleDTO rule, Expression where) {
        Column column = getColumn(rule);
        Object value = getValue(rule);
        StringValue valueExpression = new StringValue(String.valueOf(value));
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(column);
        greaterThan.setRightExpression(valueExpression);
        return assemble(rule.getSpliceType(), where, greaterThan);
    }
}
