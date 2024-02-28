package com.christer.flowablestudy.listener;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-02-28 21:14
 * Description:
 * 通过监听器设置审批人
 */
public class MyListener01 implements TaskListener {
    @Override
    public void notify(final DelegateTask delegateTask) {
        System.out.println("MyListener01-----notify-----执行了" + delegateTask.getName());
        if (EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            // 用户节点的创建，然后指派审批人
            delegateTask.setAssignee("Christer");
        }
    }
}
