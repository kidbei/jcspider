package com.jcspider.server.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public class IDUtils {

    public static String genTaskId(String sourceUrl, String method) {
        return DigestUtils.md5Hex(sourceUrl + "|" + method);
    }


    public static final String genUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
