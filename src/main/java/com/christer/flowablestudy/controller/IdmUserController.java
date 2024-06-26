package com.christer.flowablestudy.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.christer.flowablestudy.util.DecryptUtil;
import com.christer.myapicommon.model.entity.DepartmentEntity;
import com.christer.myapicommon.model.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.IdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.spring.integration.Flowable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.christer.myapicommon.common.CommonConstant.*;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-03-03 21:40
 * Description:
 * 用户组管理
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class IdmUserController {

    private final IdentityService identityService;

    private final DecryptUtil decryptUtil;


    @PostMapping("/addUser")
    public ResponseEntity<Void> addUserInfo(@RequestBody String encrypt, HttpServletRequest request) {
        log.info("flowable 同步更新用户数据");
        final String jsonStr = decryptUtil.generateRsaDecryptData(encrypt);
        if (!StringUtils.hasText(jsonStr)) {
            log.error("未能正确解密数据:{}", encrypt);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        final UserEntity userEntity = JSONUtil.toBean(jsonStr, UserEntity.class);
        if (null == userEntity) {
            log.error("无法获取用户数据:{}", jsonStr);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        final String sign = request.getHeader("sign");
        final String digestSign = decryptUtil.getDigestSign(userEntity.getUserAccount(), SALT);
        if (!CharSequenceUtil.equals(sign, digestSign)) {
            log.error("签名认证失败:{}", sign);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 新增用户
        final User user = identityService.newUser(String.valueOf(userEntity.getId()));
        user.setPassword(userEntity.getUserPassword());
        user.setFirstName(userEntity.getUserName());
        user.setLastName(userEntity.getUserAccount());
        // 模拟数据
        user.setEmail(userEntity.getUserAccount() + "@qq.com");
        user.setDisplayName(userEntity.getUserName());
        identityService.saveUser(user);
        // 普通用户，一般无工作流审核权限，所以放在普通用户组, 若为管理员后台新增的用户，可能为审核人员，需要绑定用户关系
        if (userEntity.getDepartmentId() != null) {
            identityService.createMembership(String.valueOf(userEntity.getId()), String.valueOf(userEntity.getDepartmentId()));
        } else {
            identityService.createMembership(String.valueOf(userEntity.getId()), "3");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/updateUser")
    public ResponseEntity<Void> updateUser(HttpServletRequest request) {
        log.info("flowable 同步编辑用户数据");
        final String sign = request.getHeader("sign");
        final String userId = request.getHeader("userId");
        final String departmentId = request.getHeader("departmentId");
        final String oldDepartmentId = request.getHeader("oldDepartmentId");
        final String digestSign = decryptUtil.getDigestSign(userId + ":" + departmentId, SALT);
        if (!CharSequenceUtil.equals(sign, digestSign)) {
            log.error("签名认证失败:{}", sign);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 删除旧关联
        if (StringUtils.hasText(oldDepartmentId)) {
            identityService.deleteMembership(userId, oldDepartmentId);
        }
        // 增加新关联
        identityService.createMembership(userId, departmentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 修改用户密码
     * @param userEntity
     * @return
     */
    @PutMapping("/changeUserPassword")
    public ResponseEntity<Void> changePassword(@RequestBody UserEntity userEntity) {
        log.info("flowable 同步修改用户密码");
        User user = identityService.createUserQuery().userId(String.valueOf(userEntity.getId())).singleResult();
        user.setPassword(userEntity.getUserPassword());
        identityService.updateUserPassword(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/userGroup")
    public ResponseEntity<Void> addUserGroup(@RequestBody DepartmentEntity departmentEntity, HttpServletRequest request) {
        log.info("flowable 同步新增用户组");
        final String sign = request.getHeader("sign");
        final String digestSign = decryptUtil.getDigestSign(JSONUtil.toJsonStr(departmentEntity), DEPARTMENT_SALT);
        if (!CharSequenceUtil.equals(sign, digestSign)) {
            log.error("新增用户组-签名认证失败:{}", sign);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 新增用户组
        final Group group = identityService.newGroup(String.valueOf(departmentEntity.getId()));
        group.setName(departmentEntity.getDeptName());
        identityService.saveGroup(group);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/userGroup")
    public ResponseEntity<Void> updateUserGroup(@RequestBody DepartmentEntity departmentEntity, HttpServletRequest request) {
        log.info("flowable 同步修改用户组");
        final String sign = request.getHeader("editSign");
        final String digestSign = decryptUtil.getDigestSign(JSONUtil.toJsonStr(departmentEntity), DEPARTMENT_SALT);
        if (!CharSequenceUtil.equals(sign, digestSign)) {
            log.error("编辑用户组-签名认证失败:{}", sign);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 编辑用户组
        final Group group = identityService.createGroupQuery().groupId(String.valueOf(departmentEntity.getId())).singleResult();
        group.setName(departmentEntity.getDeptName());
        identityService.saveGroup(group);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/userGroup")
    public ResponseEntity<Void> deleteUserGroup(@RequestParam("id") Long id, HttpServletRequest request) {
        log.info("flowable 同步删除用户组");
        final String sign = request.getHeader("deleteSign");
        final String digestSign = decryptUtil.getDigestSign(id.toString(), DEPARTMENT_SALT_ID);
        if (!CharSequenceUtil.equals(sign, digestSign)) {
            log.error("删除用户组-签名认证失败:{}", sign);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 删除用户组
        identityService.deleteGroup(id.toString());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
