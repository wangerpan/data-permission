package com.wep.permission.config;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.wep.permission.aspect.DataScopeAspect;
import com.wep.permission.dto.DataScopeInfo;
import com.wep.permission.dto.DataScopeRuleDTO;
import com.wep.permission.exception.PermissionException;
import com.wep.permission.strategy.ExpressStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Component
public class DataScopeHandler implements DataPermissionHandler {

    private final Expression noDataExpression = getNoDataExpression();

    @Resource
    private Map<String, ExpressStrategy> expressStrategyMap;

    @Override
    public Expression getSqlSegment(Expression oldWhere, String mappedStatementId) {
        DataScopeInfo dataScopeInfo = DataScopeAspect.getDataScopeInfo();

        // 没有配置规则的话就按照现有sql执行
        if (dataScopeInfo == null || CollectionUtil.isEmpty(dataScopeInfo.getRuleList())) {
            return oldWhere;
        }

        log.debug("----------------------------------数据权限处理器 开始处理SQL----------------------------------");
        log.debug("处理前的 WHERE 条件: {}", oldWhere);
        Expression newWhere = null;

        List<DataScopeRuleDTO> ruleList = dataScopeInfo.getRuleList();

        // 当规则只有一条且需要根据规则构造的条件为空时, 让这条sql无法查询出数据
        if (ruleList.size() == 1) {
            Expression apply = process(ruleList.stream().findFirst().get(), newWhere);
            newWhere = Objects.isNull(apply) ? this.noDataExpression : apply;
        } else {
            for (DataScopeRuleDTO rule : ruleList) {
                Expression apply = process(rule, newWhere);
                if (!Objects.isNull(apply)) {
                    newWhere = apply;
                }
            }
        }

        log.debug("数据限制的 WHERE 条件: {}", newWhere);
        if (Objects.nonNull(oldWhere)) {
            newWhere = new AndExpression(oldWhere, new Parenthesis(newWhere));
        }
        log.debug("处理后的 WHERE 条件: {}", newWhere);
        log.debug("----------------------------------数据权限处理器 处理SQL结束----------------------------------");
        return newWhere;
    }

    private Expression process(DataScopeRuleDTO rule, Expression expression) {
        ExpressStrategy strategy = expressStrategyMap.get(rule.getExpression());
        if (strategy == null) {
            throw new PermissionException("错误的表达式：" + rule.getExpression());
        }
        return strategy.apply(rule, expression);
    }

    /**
     * 构造 1!=1 的表达式
     *
     * @return
     */
    private Expression getNoDataExpression() {
        LongValue value = new LongValue(1);
        NotEqualsTo notEqualsTo = new NotEqualsTo();
        notEqualsTo.setLeftExpression(value);
        notEqualsTo.setRightExpression(value);
        return notEqualsTo;
    }
}
