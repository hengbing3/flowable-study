package com.christer.flowablestudy.bean;

import org.springframework.stereotype.Component;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-02-28 20:37
 * Description:
 * 方法表达式-设置审批人
 */
@Component
public class MyBean {

    public String getAssignee() {
        System.out.println("MyBean.getAssignee()...执行了");
        return "王五";
    }
}
