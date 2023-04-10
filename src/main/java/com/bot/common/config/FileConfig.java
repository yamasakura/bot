package com.bot.common.config;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileConfig implements InitializingBean {


    @Value("${filePath.cqHttpIp}")
    private String cqHttpIp;
    @Value("${filePath.bot}")
    private String bot;

    @Value("${filePath.check}")
    private String check;


    public static String Bot;
    public static String CqHttpIp;

    public static String Check;

    @Override
    public void afterPropertiesSet() throws Exception {
        CqHttpIp =cqHttpIp;
        Bot = bot;
        Check = check;

    }
}
