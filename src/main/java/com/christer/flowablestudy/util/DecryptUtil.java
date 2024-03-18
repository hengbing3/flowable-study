package com.christer.flowablestudy.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-03-10 22:05
 * Description:
 * 非对称加解密
 */
@Component
@Slf4j
public class DecryptUtil {


    private final ResourceLoader resourceLoader;


    private String privateKey = "";

    public DecryptUtil(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    /**
     * 生成摘要签名
     * @param param 请求参数
     * @param salt 盐值
     * @return 单向加密数据
     */
    public String getDigestSign(String param, String salt) {
        final Digester digester = DigestUtil.digester(DigestAlgorithm.SHA256);
        // 添加签名到请求头，保证传参不被修改
        return digester.digestHex(param + salt);
    }

    /**
     * 根据签名算法-生成摘要签名
     * @param param
     * @param salt
     * @param algorithm
     * @return
     */
    public String getDigestSignByDigestAlgorithm(String param, String salt, DigestAlgorithm algorithm) {
        final Digester digester = DigestUtil.digester(algorithm);
        // 添加签名到请求头，保证传参不被修改
        return digester.digestHex(param + salt);
    }

    /**
     * 解密数据
     * @param hexStr 加密密文
     * @return 解密后的json串
     */
    public String generateRsaDecryptData(String hexStr) {
        if (!StringUtils.hasText(privateKey)) {
            privateKey = readPrivateKey();
        }
        final RSA rsa = new RSA("RSA/ECB/PKCS1Padding", privateKey, null);
        byte[] decodeHex = HexUtil.decodeHex(hexStr);
        byte[] decrypt1 = rsa.decrypt(decodeHex, KeyType.PrivateKey);
        return StrUtil.str(decrypt1, CharsetUtil.CHARSET_UTF_8);
    }

    /**
     * 读取私钥资源
     * @return
     */
    public String readPrivateKey() {
        final Resource resource = resourceLoader.getResource("classpath:static/privateKey.txt");
        try (InputStream inputStream = resource.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            StringBuilder publicKey = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                publicKey.append(line);
            }
            return publicKey.toString();

        }catch (IOException e) {
            log.error("公钥文件读取失败:{}", e.getMessage());
            throw new RuntimeException("操作失败！");
        }
    }
}
