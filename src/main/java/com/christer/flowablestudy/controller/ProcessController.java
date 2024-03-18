package com.christer.flowablestudy.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.json.JSONUtil;
import com.christer.flowablestudy.entity.FlowableInfo;
import com.christer.flowablestudy.service.ProcessService;
import com.christer.flowablestudy.util.DecryptUtil;
import com.christer.flowablestudy.util.IPUtils;
import com.christer.myapicommon.model.dto.interfaceinfo.FlowableCompleteTaskParam;
import com.christer.myapicommon.model.dto.interfaceinfo.FlowableStartParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.ui.task.service.api.DeploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static cn.hutool.crypto.digest.DigestAlgorithm.SHA384;
import static com.christer.myapicommon.common.CommonConstant.FLOWABLE_SALT;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-03-15 15:03
 * Description:
 */
@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
@Slf4j
public class ProcessController {

    private final ProcessService processService;

    private final DecryptUtil decryptUtil;

    /**
     * 启动流程并完成任务
     * @param param
     * @param request
     * @return
     */
    @PostMapping("/startAndCompleteTask")
    public ResponseEntity<FlowableInfo> startAndCompleteTask(@RequestBody @Validated FlowableStartParam param, HttpServletRequest request) {
        log.info("开启流程并完成任务：{}", param);
        // 验证签名
        final String sign = request.getHeader("sign");
        if (!StringUtils.hasText(sign)) {
            log.error("为获取到签名认证,ip地址：{}, 请求参数：{}", IPUtils.getIpAddr(request), param);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 验证签名是否正确
        final String digestSign = decryptUtil.getDigestSign(JSONUtil.toJsonStr(param), FLOWABLE_SALT);
        if (!StrUtil.equals(digestSign, sign)) {
            log.error("签名认证失败,ip地址：{}, 请求参数：{}", IPUtils.getIpAddr(request), param);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 启动流程并配置申请人
        final FlowableInfo flowableInfo = processService.startAndCompleteTask(param);
        // 获取流程相关信息并返回
        return new ResponseEntity<>(flowableInfo, HttpStatus.OK);
    }

    /**
     * 完成任务
     * @param param
     * @param request
     * @return
     */
    @PostMapping("/completeTask")
    public ResponseEntity<FlowableInfo> completeTask(@RequestBody @Validated FlowableCompleteTaskParam param, HttpServletRequest request) {
        log.info("完成任务：{}", param);
        // 验证签名
        final String sign = request.getHeader("sign");
        if (!StringUtils.hasText(sign)) {
            log.error("为获取到签名认证,ip地址：{}, 请求参数：{}", IPUtils.getIpAddr(request), param);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 验证签名是否正确
        final String digestSign = decryptUtil.getDigestSignByDigestAlgorithm(JSONUtil.toJsonStr(param), FLOWABLE_SALT, DigestAlgorithm.SHA1);
        if (!CharSequenceUtil.equals(digestSign, sign)) {
            log.error("签名认证失败,ip地址：{}, 请求参数：{}", IPUtils.getIpAddr(request), param);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 完成任务
        final FlowableInfo flowableInfo = processService.completeTask(param);
        // 获取流程相关信息并返回
        return new ResponseEntity<>(flowableInfo, HttpStatus.OK);
    }

    /**
     * 获取待办任务
     * @param userId
     * @param request
     * @return
     */
    @PostMapping("/totoTask")
    public ResponseEntity<List<String>> totoTaskList(@RequestParam String userId, HttpServletRequest request) {
        log.info("获取用户对应的代办任务:{}, ip地址:{}", userId, IPUtils.getIpAddr(request));
        final List<String> processInstanceIds = processService.totoTaskList(userId);
        return new ResponseEntity<>(processInstanceIds, HttpStatus.OK);
    }

    /**
     * 获取已办任务
     * @param userId
     * @param request
     * @return
     */
    @PostMapping("/doneTask")
    public ResponseEntity<List<String>> doneTaskList(@RequestParam String userId, HttpServletRequest request) {
        log.info("获取用户已完成的任务:{},ip地址:{}", userId, IPUtils.getIpAddr(request));
        final List<String> processInstanceIds = processService.doneTaskList(userId);
        return new ResponseEntity<>(processInstanceIds, HttpStatus.OK);
    }
}
