package com.wep.permission.strategy.impl;

import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.strategy.ExpressStrategy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import org.springframework.stereotype.Service;

/**
 * @author wep  2024/6/26
 */
@Service("RIGHT_LIKE")
public class RightLikeStrategyImpl implements ExpressStrategy {

    @Override
    public Expression apply(DataScopeRuleDTO rule, Expression where) {
        StringValue valueExpression = new StringValue(getValue(rule) + "%");
        LikeExpression likeExpression = new LikeExpression();
        likeExpression.setLeftExpression(getColumn(rule));
        likeExpression.setRightExpression(valueExpression);
        return assemble(rule.getSpliceType(), where, likeExpression);
    }
}
