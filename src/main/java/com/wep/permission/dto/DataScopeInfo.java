package com.wep.permission.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataScopeInfo {
    private List<DataScopeRuleDTO> ruleList;
}
