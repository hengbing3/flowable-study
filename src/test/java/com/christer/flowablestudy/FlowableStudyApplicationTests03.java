package com.christer.flowablestudy;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = FlowableStudyApplication.class)
class FlowableStudyApplicationTests03 {

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;


    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    /**
     * 部署流程
     */
    @Test
    void deployProcess() {
//        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment
                .addClasspathResource("process/HolidayDemo2.bpmn20.xml")
                .name("候选人案例")
                .deploy(); // 部署的方法
        System.out.println(deploy.getId());
    }

    /**
     * 启动流程实例
     */
    @Test
    void startFlow() {
        // 在流程定义表中动态维护
        final String processId = "HolidayDemo2:1:e9104181-d83f-11ee-902b-d0abd5b04905";
        // 我们创建流程图的时候自定义的，注意保证唯一
        final String processKey = "FirstFlow";
        // 设置的变量在runtimeService 里是全局变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("candidate1", "zhangsan");
        variables.put("candidate2", "lisi");
        variables.put("candidate3", "wangwu");
        // 1.根据流程定义ID启动流程实例
        runtimeService.startProcessInstanceById(processId, variables);
        // 2.根据流程定义Key启动流程实例
//        runtimeService.startProcessInstanceByKey(processKey);
    }


    /**
     * 根据候选人，查询代办信息
     * 候选人还不是审批人
     * 候选人需要通过拾取操作 把候选人变为审批人
     * 多个候选人， 只有一个可以变为审批人---拾取的操作
     * 审批人如果不想审批了。可以归还 从 审批人---> 候选人
     */
    @Test
    void claimTask() {
        // 任务实例操作我们都是通过TaskService 来实现的
//        TaskService taskService = processEngine.getTaskService();
        final List<Task> list = taskService.createTaskQuery()
                .taskCandidateUser("zhangsan")
                .list();
        for (final Task task : list) {
            // 拾取的操作 把zhangsan 由候选人变更为审批人
            taskService.claim(task.getId(), "zhangsan");
//            System.out.println("task.getClaimTime() = " + task.getClaimTime());
        }
    }

    /**
     * 任务的审批
     */
    @Test
    void completeTask() {
//        a4a17f81-d563-11ee-a695-d0abd5b04905
        // c9500954-d566-11ee-ab39-d0abd5b04905
        Map<String, Object> variables = new HashMap<>();
        variables.put("varTask2", "varTask2");
        // 完成任务的审批，根据任务的ID, 绑定对应的表达式的值
        taskService.complete("86298abf-d649-11ee-9f92-d0abd5b04905", variables);
    }

    /**
     * 流程的挂起和激活
     */
    @Test
    void suspendedActivity() {
        final String processDefinitionId = "FirstFlow:1:60774fc6-d561-11ee-a396-d0abd5b04905";
        // 做流程的挂起和激活操作 --> 针对的流程定义
        final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        // 做流程的挂起和激活操作 --> 针对流程的定义
        final boolean suspended = processDefinition.isSuspended();
        if (suspended) {
            System.out.println("激活流程");
            repositoryService.activateProcessDefinitionById(processDefinitionId);
        } else {
            System.out.println("挂起流程");
            repositoryService.suspendProcessDefinitionById(processDefinitionId);
        }
    }

    /**
     * 挂起流程实例
     */
    @Test
    void suspendInstance() {

        // 挂起流程实例
        runtimeService.suspendProcessInstanceById("f784f5d0-d575-11ee-a44a-d0abd5b04905");
        // 激活流程实例
//        runtimeService.activateProcessInstanceById("f784f5d0-d575-11ee-a44a-d0abd5b04905");
        // 查询任务状态
        final Task task = taskService.createTaskQuery()
                .processInstanceId("f784f5d0-d575-11ee-a44a-d0abd5b04905")
                .singleResult();
        final boolean suspended = task.isSuspended();
        // false: 激活 true: 挂起
        System.out.println("流程实例状态：" + suspended);
    }


}
