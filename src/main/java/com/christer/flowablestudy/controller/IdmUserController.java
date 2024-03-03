package com.christer.flowablestudy.controller;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.IdentityService;
import org.flowable.http.common.api.HttpResponse;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-03-03 21:40
 * Description:
 * 用户组管理
 */
@RestController
@RequiredArgsConstructor
public class IdmUserController {

    private final IdentityService identityService;

    @PostMapping("/userInfo")
    public ResponseEntity<Void> addUserInfo(HttpServletRequest request) {
        // 新增用户
        final User user = identityService.newUser("111");
        user.setPassword("111");
        user.setDisplayName("111");
        identityService.saveUser(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/userGroup")
    public ResponseEntity<Void> addUserGroup(@RequestParam Map<String , String> params, HttpServletRequest request) {
        // 新增用户
        final String deptId = params.get("deptId");
        final String deptName = params.get("deptName");
        final String type = params.get("type");
        final Group group = identityService.newGroup(deptId);
        group.setName(deptName);
        group.setType(type);
        identityService.saveGroup(group);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
