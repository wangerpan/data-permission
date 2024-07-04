package com.wep.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CrmCustomerDTO {
    // 农户Id
    private String id;
    // 农户手机号
    private String cusPhone;
    // 农户姓名
    private String cusName;
    // 农户地址
    private String address;
    // 农户身份号
    private String idCard;
    // 下级农户
    private CrmCustomerDTO childCustomer;
    // 农户田块id
    private List<String> landIds;
}
