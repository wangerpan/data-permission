package com.wep.permission.controller;

import cn.hutool.core.collection.CollUtil;
import com.wep.permission.annotation.DataObjPermission;
import com.wep.permission.annotation.DataScope;
import com.wep.permission.annotation.DataScopeExpression;
import com.wep.permission.annotation.FieldPermission;
import com.wep.permission.dto.CrmCustomerDTO;
import com.wep.permission.enums.ExpressionEnum;
import com.wep.permission.enums.SpliceTypeEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 样例
 */
@DataObjPermission(roleKeys = {"information"})
@RestController
@RequestMapping("/permission")
public class TestPermissionFacade {

    @FieldPermission(viewFields = {"cusPhone", "cusName", "address", "idCard"}, maskFields = {"cusPhone", "idCard"})
    @DataScope(dataScopeExpressions = {
            @DataScopeExpression(tableAlias = "address", columnName = "address", value = "沛县", expression = ExpressionEnum.LIKE, spliceType = SpliceTypeEnum.OR)
            , @DataScopeExpression(tableAlias = "address", columnName = "address", value = "朱寨镇", expression = ExpressionEnum.LIKE, spliceType = SpliceTypeEnum.OR)
            , @DataScopeExpression(tableAlias = "address", columnName = "create_user", value = "com.wep.permission.utils.TokenUtils#getCurrentUserId", expression = ExpressionEnum.EQ)})
    @GetMapping("/list")
    public ResponseEntity<List<CrmCustomerDTO>> list() {

        CrmCustomerDTO childCustomer = CrmCustomerDTO.builder().id("912").cusPhone("1361512456").cusName("祝小小")
                .idCard("320324199210086897").address("江苏省徐州市沛县杨屯镇后屯").build();

        CrmCustomerDTO customerDTO1 = CrmCustomerDTO.builder().id("913").cusPhone("13615122332").cusName("祝老板")
                .idCard("320324196006245914").address("江苏省徐州市沛县杨屯镇后屯").childCustomer(childCustomer)
                .landIds(CollUtil.toList("1771080323195498497", "1768485550987177985")).build();

        CrmCustomerDTO customerDTO2 = CrmCustomerDTO.builder().id("1791").cusPhone("15805227778").cusName("程大哥")
                .idCard("230827197112170038").address("江苏省徐州市沛县朱寨镇陈楼村").childCustomer(childCustomer)
                .landIds(CollUtil.toList("1770698493045672721", "1764833413456796993", "1762307445678486529")).build();
        List<CrmCustomerDTO> crmCustomerDTOList = CollUtil.toList(customerDTO1, customerDTO2);
        return ResponseEntity.ok(crmCustomerDTOList);
    }
}
