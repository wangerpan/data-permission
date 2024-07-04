# Data-Permission

#### 介绍
数据权限控制组件
控制粒度:
可以根据当前登录人的信息,控制数据对象是否可以进行访问.
可以根据注解权限控制到具体字段的查看,编辑,脱敏等.
可以根据注解实现数据权限的动态控制,对现有业务不需要进行改造

#### 软件架构
软件架构说明
springboot3.2.7
JDK17



#### 安装教程
1. 打包: mvn clean -DskipTests=true install
2. 把jar引入到自己的项目中.


#### 使用说明
 
核心注解:@DataObjPermission  @FieldPermission @DataScope

1. @DataObjPermission. 数据对象权限控制:角色,用户id,接口的请求方式,自定义控制的接口列表  结合TokenUtils获取当前登录人信息,这个可以根据自己的登录框架进行修改与调整
```
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface DataObjPermission {
    /**
     * 是否必须
     * 默认 是
     */
    boolean required() default true;

    /**
     * 哪些角色可以访问
     */
    String[] roleKeys() default {};


    /**
     * 哪些用户可以访问
     */
    String[] userIds() default {};

    /**
     * 是否通过请求方式进行权限验证
     * 如果是,则优先使用这个方式进行权限验证
     * <p>
     * 默认 否
     */
    boolean isRequestType() default false;

    /**
     * 请求方式
     */
    String[] requestTypes() default {};

    /**
     * 定义需要进行权限验证的路径
     * <p>
     * 例如:dept模块下,urls={"/list","/get"}访问这两个接口需要进行权限配置,否则无法访问
     */
    String[] urls() default {};
}
```

2. @FieldPermission. 字段权限控制 控制接口返回的字段.
```
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface FieldPermission {
    boolean required() default true;

    /**
     * 定义被查看的字段列表
     */
    String[] viewFields() default {};

    /**
     * 定义脱敏的字段列表
     * 一般用于的字段:手机号,身份照,邮箱,地址,合同编号的等
     */
    String[] maskFields() default {};

    /**
     * 定义可编辑的字段列表
     */
    String[] editFields() default {};
}

```
3. @DataScope 数据范围权限控制,可以根据表中的字段进行控制 注意:在@DataScope 定义的多个@DataScopeExpression,会根据顺依次拼接条件
使用样例
````
@DataScope(dataScopeExpressions = {
            @DataScopeExpression(tableAlias = "address", columnName = "address", value = "沛县", expression = ExpressionEnum.LIKE)
            , @DataScopeExpression(tableAlias = "address", columnName = "address", value = "朱寨镇", expression = ExpressionEnum.LIKE)})
样例实现的逻辑: 会在原有的sql插叙条件添加 and (address.address like '%沛县%' and address.address like '%朱寨镇%' )
````
````
@DataScope(dataScopeExpressions = {
            @DataScopeExpression(tableAlias = "address", columnName = "address", value = "沛县", expression = ExpressionEnum.LIKE, spliceType = SpliceTypeEnum.OR)
            , @DataScopeExpression(tableAlias = "address", columnName = "address", value = "朱寨镇", expression = ExpressionEnum.LIKE, spliceType = SpliceTypeEnum.OR)})
            
样例实现的逻辑: 会在原有的sql插叙条件添加 OR (address.address like '%沛县%' OR address.address like '%朱寨镇%' )
````
````
@DataScope(dataScopeExpressions = {
            @DataScopeExpression(tableAlias = "address", columnName = "address", value = "沛县", expression = ExpressionEnum.LIKE, spliceType = SpliceTypeEnum.OR)
            , @DataScopeExpression(tableAlias = "address", columnName = "address", value = "朱寨镇", expression = ExpressionEnum.LIKE, spliceType = SpliceTypeEnum.OR)
            , @DataScopeExpression(tableAlias = "address", columnName = "create_user", value = "com.wep.permission.utils.TokenUtils#getCurrentUserId", expression = ExpressionEnum.EQ)})
样例实现的逻辑: 会在原有的sql插叙条件添加 OR (address.address like '%沛县%' OR address.address like '%朱寨镇%' and address.create_user = 当前用户id)             
````

#### 感谢

https://gitee.com/whzzone/wonder-server
