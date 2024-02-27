package com.christer.flowablestudy;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class FlowableStudyApplicationTests {

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
                .addClasspathResource("process/FirstFlow.bpmn20.xml")
                .name("第二个流程图")
                .deploy(); // 部署的方法
        System.out.println(deploy.getId());
    }

    /**
     * 启动流程实例
     */
    @Test
    void startFlow() {
        // 在流程定义表中动态维护
        final String processId = "FirstFlow:1:60774fc6-d561-11ee-a396-d0abd5b04905";
        // 我们创建流程图的时候自定义的，注意保证唯一
        final String processKey = "FirstFlow";
        // 1.根据流程定义ID启动流程实例
//        runtimeService.startProcessInstanceById(processId);
        // 2.根据流程定义Key启动流程实例
        runtimeService.startProcessInstanceByKey(processKey);
    }

    /**
     * 根据用户查询代办信息
     */
    @Test
    void findFlow() {
        // 任务实例操作我们都是通过TaskService 来实现的
//        TaskService taskService = processEngine.getTaskService();
        final List<Task> list = taskService.createTaskQuery()
                .taskAssignee("zhangsan")
                .list();
        for (final Task task : list) {
            System.out.println(task.getId());
        }
    }



}
