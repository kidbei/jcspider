package com.jcspider.server.utils;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class IPUtils {


    public static final String getLocalIP() throws UnknownHostException {
        return Inet4Address.getLocalHost().getHostAddress();
    }


}
