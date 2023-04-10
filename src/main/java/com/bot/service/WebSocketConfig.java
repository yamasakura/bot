package com.bot.service;

import com.alibaba.fastjson.JSONObject;
import com.bot.common.config.FileConfig;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Component
public class WebSocketConfig {

    @Resource
    private BotService botService;
    public Boolean flag = false;


    @Bean
    public WebSocketClient webSocketClient() {
        try {
            String URI = "ws://" + FileConfig.CqHttpIp + ":5701";
            log.info("链接地址：" + URI);
            WebSocketClient webSocketClient = new WebSocketClient(new URI(URI), new Draft_6455()) {

                @Override
                public void onOpen(ServerHandshake handshakeData) {
                    log.info("[websocket] 连接成功");
                }

                @Override
                public void onMessage(String message) {

                    JSONObject qqMessage = JSONObject.parseObject(message);
                    if (qqMessage.get("raw_message") != null) {
                        String raw_message = qqMessage.getString("raw_message").trim();

                        long group_id = Long.parseLong(qqMessage.getString("group_id"));
                      group_id = 562528726; //测试群
//                      group_id =  938710832;
//                      group_id =  761817128;  //调用区

                        int message_id = Integer.parseInt(qqMessage.getString("message_id"));

                        if (raw_message.endsWith("专精") || raw_message.endsWith("材料") || raw_message.endsWith("精二")) {
                            botService.sendImage(group_id, raw_message.substring(0, raw_message.length() - 2), "skill");
                        }
                        if (raw_message.endsWith("模组")) {
                            botService.sendImage(group_id, raw_message.substring(0, raw_message.length() - 2), "mod");
                        }
                        if (raw_message.endsWith("技能")) {
                            botService.sendImage(group_id, raw_message.substring(0, raw_message.length() - 2), "char");
                        }
                        if (raw_message.startsWith("材料一图流") || raw_message.startsWith("材料掉率")) {
                            botService.sendItemImg("", group_id);
                        }
                        if (raw_message.startsWith("活动一图流") || raw_message.startsWith("活动掉率") || raw_message.startsWith("活动材料")) {
                            botService.sendItemImg("act", group_id);
                        }
                        if (raw_message.startsWith(".攒抽")) {
                            botService.gachaCal(group_id);
                        }
                        if (raw_message.startsWith("help")) {
                            String helpMessage = "可用命令格式：\n干员名模组\n干员名技能\n干员名专精" +
                                    "\n查看材料掉率：材料一图流、活动一图流" +
                                    "\n发送 .攒抽  获得下个限定池前还有多少抽" +
                                    "\n直接发送游戏截图查询公招组合";
                            botService.sendMessage(group_id, helpMessage);
                        }

//
                        if (raw_message.startsWith("[CQ:image")) {
                            String substring = raw_message.substring(raw_message.indexOf("=") + 1, raw_message.indexOf(",subType"));
                            boolean flag = botService.imageOcr(substring, group_id);
                            if (flag) {

                            }
                        }

                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("[websocket] 退出连接");
                    flag = true;
                }

                @Override
                public void onError(Exception ex) {
//                    log.info("[websocket] 连接错误={}",ex.getMessage());
                }
            };

            webSocketClient.connect();

            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (flag) {
                        try {
                            webSocketClient.reconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 2000, 5000);//5秒执行一次 然后休息2秒

            return webSocketClient;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}