package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5加密
    // 该算法只能加密，不能解密；同样的密码每次对应的加密结果是相同的；
    // 在每位用户的密码后面加一个随机字符串后再加密以防止密码被盗
    public static String md5(String key){
        if(StringUtils.isBlank(key))
            return null;
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
