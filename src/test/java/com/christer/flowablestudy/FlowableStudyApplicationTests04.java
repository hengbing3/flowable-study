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
import java.util.List;

/**
 * 候选人组案例讲解
 */
@SpringBootTest(classes = FlowableStudyApplication.class)
class FlowableStudyApplicationTests04 {

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
     * 维护用户
     */
    @Test
    void createUser() {
        final User user = identityService.newUser("lisi");
        user.setEmail("lisi@qq.com");
        user.setFirstName("si");
        user.setLastName("li");
        user.setPassword("123");
        identityService.saveUser(user);
    }

    /**
     * 用户组维护
     */
    @Test
    void createGroup() {
        final Group group = identityService.newGroup("3");
        group.setName("普通用户组");
        group.setType("type3");
        identityService.saveGroup(group);
    }

    /**
     * 维护用户和用户组的关联关系
     */
    @Test
    void createMemberShip() {
        final Group group = identityService.createGroupQuery().groupId("xsb").singleResult();
        final List<User> users = identityService.createUserQuery().list();
        for (final User user : users) {
            // 用户和组的一个绑定
            identityService.createMembership(user.getId(), group.getId());
        }
    }

    /**
     * 部署流程
     */
    @Test
    void deployProcess() {
//        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment
                .addClasspathResource("process/HolidayDemo3.bpmn20.xml")
                .name("候选人组案例")
                .deploy(); // 部署的方法
        System.out.println(deploy.getId());
    }

    /**
     * 启动流程实例
     */
    @Test
    void startFlow() {
        // 在流程定义表中动态维护
        final String processId = "HolidayDemo3:1:40eaf29c-d870-11ee-a488-d0abd5b04905";
        // 1.根据流程定义ID启动流程实例
        runtimeService.startProcessInstanceById(processId);

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

    @Test
    void completeTask() {
        taskService.complete("b30b6126-d873-11ee-94f4-d0abd5b04905");
    }


}
