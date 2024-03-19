package com.christer.flowablestudy.service.impl;

import com.christer.flowablestudy.entity.FlowableInfo;
import com.christer.flowablestudy.service.ProcessService;
import com.christer.myapicommon.model.dto.interfaceinfo.FlowableCompleteTaskParam;
import com.christer.myapicommon.model.dto.interfaceinfo.FlowableStartParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.Group;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-03-15 15:43
 * Description:
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final IdentityService identityService;

    private final HistoryService historyService;

    @Override
    public FlowableInfo startAndCompleteTask(final FlowableStartParam param) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("applyUser", param.getAssigneeUser());
        // 启动流程，并选择分配人
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(param.getProcessInstanceKey(), variables);
        log.info("流程启动成功，流程实例ID：{}", processInstance.getId());
        // 根据流程实例id获取任务信息
        final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
                .taskAssignee(param.getAssigneeUser())
                .singleResult();
        taskService.complete(task.getId());
        log.info("任务完成，任务ID：{}", task.getId());
        // 封装并返回相关信息
        final FlowableInfo flowableInfo = new FlowableInfo();
        flowableInfo.setAssignee(param.getAssigneeUser());
        flowableInfo.setProcessInstanceId(processInstance.getId());
        flowableInfo.setTaskName(task.getName());
        flowableInfo.setTaskId(task.getId());
        return flowableInfo;
    }

    @Override
    public List<String> totoTaskList(final String userId) {
        // 根据当前登录的用户来查询
        final Group group = identityService.createGroupQuery()
                .groupMember(userId)
                .singleResult();
        if (null == group) {
            return Collections.emptyList();
        }
        // 获取任务列表
        final List<Task> list = taskService.createTaskQuery()
                .taskCandidateGroup(group.getId())
                .list();
        // 获取任务关联的流程实例id
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : list.stream().map(TaskInfo::getProcessInstanceId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> doneTaskList(final String userId) {
        // 根据用户id获取用户组
        final Group group = identityService.createGroupQuery()
                .groupMember(userId)
                .singleResult();
        if (null == group) {
            return Collections.emptyList();
        }
        // 查询已办任务，包括已完成的任务，自己所属组中已完成的任务，自己指派的任务
        final List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .or()
                .finished()
                .taskCandidateGroup(group.getId())
                .taskAssignee(userId)
                .endOr()
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(HistoricTaskInstance::getProcessInstanceId).distinct().collect(Collectors.toList());
    }

    @Override
    public FlowableInfo completeTask(final FlowableCompleteTaskParam param) {
        // TODO 目前只对API开放平台生效，实际上的业务需求更复杂
        // 根据是否存在审核结果，采取不同的执行方式
        if (StringUtils.hasText(param.getAuditResult())) {
            // 获取用户组
            final Group group = identityService.createGroupQuery()
                    .groupMember(param.getCandidateGroupUser())
                    .singleResult();
            if (null == group) {
                throw new RuntimeException("未找到用户组！");
            }
            final Task task = taskService.createTaskQuery()
                    .processInstanceId(param.getProcessInstanceId())
                    .taskCandidateGroup(group.getId())
                    .singleResult();
            if (task == null) {
                throw new RuntimeException("任务不存在!");
            }
            // 任务拾取
            taskService.claim(task.getId(), param.getCandidateGroupUser());
            final HashMap<String, Object> variables = new HashMap<>();
            variables.put("auditResult",param.getAuditResult());
            // 完成任务
            taskService.complete(task.getId(), variables);
            log.info("任务完成，任务ID：{}", task.getId());
            // 封装并返回相关信息
            final FlowableInfo flowableInfo = new FlowableInfo();
            flowableInfo.setAssignee(param.getCandidateGroupUser());
            flowableInfo.setProcessInstanceId(flowableInfo.getProcessInstanceId());
            flowableInfo.setTaskName(task.getName());
            flowableInfo.setTaskId(task.getId());
            return flowableInfo;

        } else {
            final Task task = taskService.createTaskQuery()
                    .processInstanceId(param.getProcessInstanceId())
                    .taskAssignee(param.getAssigneeUser())
                    .singleResult();
            if (task == null) {
                throw new RuntimeException("任务不存在!");
            }
            taskService.complete(task.getId());
            log.info("任务完成，任务ID：{}", task.getId());
            // 封装并返回相关信息
            final FlowableInfo flowableInfo = new FlowableInfo();
            flowableInfo.setAssignee(param.getAssigneeUser());
            flowableInfo.setProcessInstanceId(flowableInfo.getProcessInstanceId());
            flowableInfo.setTaskName(task.getName());
            flowableInfo.setTaskId(task.getId());
            return flowableInfo;
        }
    }
}
