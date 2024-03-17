package com.christer.flowablestudy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-02-15 18:12
 * Description:
 */
public class IPUtils {
    private static final Logger logger = LoggerFactory.getLogger(IPUtils.class);
    private static final String IP_UTILS_FLAG = ",";
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IP1 = "127.0.0.1";
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Original-Forwarded-For",
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    /**
     * 获取IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        for (String header : IP_HEADER_CANDIDATES) {
            ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                break;
            }
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
                try {
                    InetAddress iNet = InetAddress.getLocalHost();
                    ip = iNet.getHostAddress();
                } catch (UnknownHostException e) {
                    logger.error("IPUtils - Unable to retrieve localhost IP", e);
                }
            }
        }

        if (StringUtils.hasText(ip) && ip.contains(IP_UTILS_FLAG)) {
            ip = ip.substring(0, ip.indexOf(IP_UTILS_FLAG));
        }
        return ip;
    }
}
