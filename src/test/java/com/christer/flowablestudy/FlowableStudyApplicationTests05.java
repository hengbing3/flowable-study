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
                .addClasspathResource("process/HolidayDemo6.bpmn20.xml")
                .name("包容网关案例")
                .deploy(); // 部署的方法
        System.out.println(deploy.getId());
    }

    /**
     * 启动流程实例
     */
    @Test
    void startFlow() {
        // 在流程定义表中动态维护
        final String processDefinitionId = "HolidayDemo6:1:2a11ffad-d880-11ee-bb95-d0abd5b04905";
        // 1.根据流程定义ID启动流程实例
        runtimeService.startProcessInstanceById(processDefinitionId);

    }

    /**
     * 删除流程部署
     */
    @Test
    void deleteDeployFlow() {
        repositoryService.deleteDeployment("96d25fbf-e07c-11ee-9ba4-d0abd5b04902", true);
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
        final String taskId = "cef218be-d880-11ee-b6be-d0abd5b04905";
        // 绑定请假的天数，让网关做选择
        Map<String ,Object> map = new HashMap<>();
        map.put("num", 5);
        taskService.complete(taskId, map);
    }


}
