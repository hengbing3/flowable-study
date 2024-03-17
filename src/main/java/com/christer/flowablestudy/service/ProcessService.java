package com.christer.flowablestudy.service;

import com.christer.flowablestudy.entity.FlowableInfo;
import com.christer.myapicommon.model.dto.interfaceinfo.FlowableCompleteTaskParam;
import com.christer.myapicommon.model.dto.interfaceinfo.FlowableStartParam;

import java.util.List;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-03-15 15:42
 * Description:
 */
public interface ProcessService {
    /**
     * 启动流程并完成任务
     * @param param
     * @return
     */
    FlowableInfo startAndCompleteTask(FlowableStartParam param);

    /**
     * 查询用户待办任务列表
     * @param userId 用户id
     * @return
     */
    List<String> totoTaskList(String userId);

    /**
     * 查询用户已办任务列表
     * @param userId
     * @return
     */
    List<String> doneTaskList(String userId);

    /**
     * 完成任务
     * @param param
     * @return
     */
    FlowableInfo completeTask(FlowableCompleteTaskParam param);
}
