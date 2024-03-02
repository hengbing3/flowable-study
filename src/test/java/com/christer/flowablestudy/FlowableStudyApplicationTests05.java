package com.christer.flowablestudy;

import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关
 */
@SpringBootTest(classes = FlowableStudyApplication.class)
class FlowableStudyApplicationTests05 {

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;


    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private IdentityService identityService;



    /**
     * 部署流程
     */
    @Test
    void deployProcess() {
//        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment
                .addClasspathResource("process/HolidayDemo4.bpmn20.xml")
                .name("排他网关的案例")
                .deploy(); // 部署的方法
        System.out.println(deploy.getId());
    }

    /**
     * 启动流程实例
     */
    @Test
    void startFlow() {
        // 在流程定义表中动态维护
        final String processDefinitionId = "HolidayDemo4:1:4d0270c0-d876-11ee-a519-d0abd5b04905";
        // 1.根据流程定义ID启动流程实例
        runtimeService.startProcessInstanceById(processDefinitionId);

    }

    /**
     * 候选人组查询
     */
    @Test
    void findCandidateTask() {
        // 根据当前登录的用户来查询
        final Group group = identityService.createGroupQuery()
                .groupMember("zhangsan")
                .singleResult();
        System.out.println("group.getId() = " + group.getId());
        // 根据候选人组查询任务列表
        final List<Task> list = taskService.createTaskQuery()
                .taskCandidateGroup(group.getId())
                .list();
        for (final Task task : list) {
            System.out.println("task.getId() = " + task.getId());
        }
    }

    /**
     * 任务拾取
     */
    @Test
    void claimTask() {
        // 根据当前登录的用户来查询
        final Group group = identityService.createGroupQuery()
                .groupMember("zhangsan")
                .singleResult();
        System.out.println("group.getId() = " + group.getId());
        // 根据候选人组查询任务列表
        final List<Task> list = taskService.createTaskQuery()
                .processDefinitionId("HolidayDemo3:1:40eaf29c-d870-11ee-a488-d0abd5b04905")
                .taskCandidateGroup(group.getId())
                .list();
        for (final Task task : list) {
            taskService.claim(task.getId(), "zhangsan");
        }
    }

    /**
     * 任务审批
     */
    @Test
    void completeTask() {
        final String taskId = "35060982-d877-11ee-af9e-d0abd5b04905";
        // 绑定请假的天数，让网关做选择
        Map<String ,Object> map = new HashMap<>();
        map.put("num", 5);
        taskService.complete(taskId, map);
    }


}
