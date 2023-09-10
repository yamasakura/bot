package com.bot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bot.common.config.FileConfig;
import com.bot.common.util.FileUtil;
import com.bot.common.util.HttpRequestUtil;
import com.bot.service.vo.ItemVo;
import com.bot.service.vo.PenguinDataResponseVo;
import com.bot.service.vo.RecruitData;
import com.bot.service.vo.RecruitResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BotService {


    public void sendImage(long group_id, String message, String imageType) {
        JSONObject charNameTable = JSONObject.parseObject(FileUtil.read(FileConfig.Bot + "charNameTable.json"));

        if (charNameTable.get(message) == null) return;
        String charName = charNameTable.getString(message);
        List<String> pathList = new ArrayList<>();
        pathList.add("char/char_" + charName);

        if ("skill".equals(imageType)) {
            pathList.clear();
            pathList.add("skill/skill_" + charName);
        }

        if ("mod".equals(imageType)) {
            JSONObject modTable = JSONObject.parseObject(FileUtil.read(FileConfig.Bot + "modTable.json"));
            if (modTable.get(charName) == null) return;
            JSONArray jsonArray = JSONArray.parseArray(modTable.getString(charName));
            pathList.clear();
            jsonArray.forEach(modId -> pathList.add("mod/" + charName + "_" + modId ));
        }

        for (String urlPath : pathList) {
            String url = "http://" + FileConfig.CqHttpIp + ":5700/send_group_msg?group_id=" + group_id + "&message=" +
                    "[CQ:image,file=file:/web/static/bot/" + urlPath + ".png,subType=0,cache=0]";
//            log.info("https://yituliu.site/bot/" + urlPath);
            String result = HttpRequestUtil.doGet(url, new HashMap<>());
            log.info("发送成功:==>{}", result);
        }


    }

    public void sendItemImg(String type, long group_id) {
        // user_id 为QQ好友QQ号
        String path = new SimpleDateFormat("yyyyMMdd").format(new Date());
        if ("act".equals(type)) path = type;
        String url = "http://" + FileConfig.CqHttpIp + ":5700/send_group_msg?group_id=" + group_id + "&message=" +
                "[CQ:image,file=file:/web/static/bot/item/" + path + ".png,subType=0,cache=0]";

        String result = HttpRequestUtil.doGet(url, new HashMap<>());
        sendMessage(group_id, "https://yituliu.site/");
        log.info("发送成功:==>{}", result);
    }





    public void wbSendByPhone(List<Long> group_ids) {
        SimpleDateFormat simpleDateFormat_today = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String format_system = simpleDateFormat_today.format(new Date()); //获取系统日期
//        format_system = "2023-01-01";
        String url = "https://m.weibo.cn/api/container/getIndex?containerid=1076036279793937";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("MWeibo-Pwa", "1");
        hashMap.put("Referer", "https://m.weibo.cn/u/6279793937");
        hashMap.put("X-Requested-With", "XMLHttpRequest");
        hashMap.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/108.0.0.0");

        String wbStr = HttpRequestUtil.doGet(url, hashMap);    //微博动态初始数据
        JSONArray wbData = JSONArray.parseArray(JSONObject.parseObject(JSONObject.parseObject(wbStr)
                .getString("data")).getString("cards"));  //微博动态数组

        JSONObject cakeJson = JSONObject.parseObject(FileUtil.read(FileConfig.Bot + "cake.json"));   //每日已发送动态日志文件

        String jsonDate = cakeJson.getString("date");  //上次保存的时间获取
        JSONObject ids = JSONObject.parseObject(cakeJson.getString("ids"));//如果日志时间等于今天则不需要默认值 //获取发送过的动态id


        HashMap<Object, Object> map_cake = new HashMap<>();    //日志结果
        map_cake.put("date", format_system);

        String htmlRegex = "<[^>]+>";

        for (Object cards : wbData) {  //微博消息数组循环

            JSONObject wb_card = JSONObject.parseObject(cards.toString());
            JSONObject blog = JSONObject.parseObject(wb_card.getString("mblog"));

            String cst = blog.getString("created_at");//获取动态时间
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US); // 格式转换
            Date date = null;
            try {
                date = sdf.parse(cst);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String format_wb = simpleDateFormat_today.format(date);

            if (!format_system.equals(format_wb)) continue;//判断是否是今天

            String text_raw = blog.getString("text");//动态内容

            text_raw = text_raw.replaceAll(htmlRegex, "");
            String mid = blog.getString("mid");//动态id
            if (ids.get(mid) != null) continue;
            ids.put(mid, mid);

            List<HashMap<Object, Object>> groupMessage = new ArrayList<>();  //QQ转发消息数组
            String message = "";
            message = message + text_raw;
            HashMap<Object, Object> messageMap = getMessageMap(message, false);
            groupMessage.add(messageMap);

            if (blog.getString("pics") != null) {
                JSONArray pics = JSONArray.parseArray(blog.getString("pics"));
                for (Object pic_item : pics) {
                    JSONObject json_pic = JSONObject.parseObject(pic_item.toString());
                    JSONObject large_pic = JSONObject.parseObject(json_pic.getString("large"));
                    String pic_url = large_pic.getString("url");
                    String message_pic = "";
                    if (pic_url.contains("gif")) {
                        message_pic = "[CQ:image,file=111.gif,subType=0,url=" + pic_url + ",cache=0]";
                    } else {
                        message_pic = "[CQ:image,file=111.jpg,subType=0,url=" + pic_url + ",cache=0]";
                    }
                    HashMap<Object, Object> messageMap_pic = getMessageMap(message_pic, true);
                    groupMessage.add(messageMap_pic);
                }
            }


            for (Long group_id : group_ids) {
                sendMessage(group_id, "Arknights发布了动态");
                sendGroupMessage(group_id, JSON.toJSONString(groupMessage));
            }

        }

        map_cake.put("ids", ids);
        FileUtil.save(FileConfig.Bot, "cake.json", JSONObject.toJSONString(map_cake));
    }

    public void gachaCal(long group_id) {

        int originium = 0; //源石
        int orundum = 0;//合成玉
        int permit = 0; //寻访
        int permit10 = 0; //十连寻访
        int remainingWeeks = 0;
        int remainingCheckinTimes = 0;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat daySdf = new SimpleDateFormat("dd");
        SimpleDateFormat monthSdf = new SimpleDateFormat("MM");
        String endTime = "2023-11-01 16:00:00";
        Date endDate = null;

//        String cakeStr = ReadFileUtil.readFile(botFilePath + "honeyCake.json"); //预测活动奖励
//        JSONArray honeyCakeList = JSONArray.parseArray(cakeStr);
//        assert honeyCakeList != null;
//        for(Object obj:honeyCakeList){
//            JSONObject jsonObject = JSONObject.parseObject(obj.toString());
//            originium += Integer.parseInt(jsonObject.getString("originium"));
//            orundum += Integer.parseInt(jsonObject.getString("orundum"));
//            permit += Integer.parseInt(jsonObject.getString("permit"));
//            permit10 += Integer.parseInt(jsonObject.getString("permit10"));
//        }

        try {
            endDate = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        long startTimeStamp = new Date().getTime();
        long endTimeStamp = endDate.getTime();
        int days = (int) ((endTimeStamp - startTimeStamp) / 86400 / 1000);


        Calendar c = Calendar.getInstance();
        String month_now = monthSdf.format(new Date());
        int months = 0;
        for (int i = 1; i < (days + 1); i++) {
            Date nextDate = new Date(startTimeStamp + 86400L * 1000 * i);
            c.setTime(nextDate);
            int weekday = c.get(Calendar.DAY_OF_WEEK);
            String nextMonth = monthSdf.format(nextDate);
            if (!month_now.equals(nextMonth)) {
                month_now = nextMonth;
                months++;
            }
            if (2 == weekday) remainingWeeks++;
            String today = daySdf.format(nextDate);
            if ("17".equals(today)) remainingCheckinTimes++;
        }

//        System.out.println(remainingWeeks*1800);
//        System.out.println(remainingWeeks*500);
//        System.out.println(days * 100);
//        System.out.println(months*600);
//        System.out.println(months*4);
//        System.out.println(remainingCheckinTimes);

        orundum += remainingWeeks * (1800 + 500) + days * 100 + months * 600;
        permit += remainingCheckinTimes + months * 4;

        int gachaTime = (int) (originium * 0.3 + orundum / 600 + permit + permit10 * 10);


        String resultText = "距离下次限定池还有" + days + "天";
        resultText = resultText + "\n还可获得" + gachaTime + "抽";
        resultText = resultText + "\n源石：" + originium + "颗";
        resultText = resultText + "\n合成玉：" + orundum + "颗";
        resultText = resultText + "\n单抽：" + permit + "张";
//        resultText = resultText + "\n十连："+permit10+"张";
        resultText = resultText + "\n以上仅为日常奖励，详细数据请移步攒抽计算器";

        sendMessage(group_id, resultText);
        sendMessage(group_id, "https://yituliu.site/gachaCal/");
    }

    public boolean imageOcr(String messageId, long group_id) {

        String url = "http://" + FileConfig.CqHttpIp + ":5700/ocr_image?image=" + messageId;
        String result = HttpRequestUtil.doGet(url, new HashMap<>());
        JSONObject jsonString = JSONObject.parseObject(result);
        Object status = jsonString.get("status");

        if (!"failed".equals(status.toString())) {

            String str = FileUtil.read(FileConfig.Bot + "tag.json");
            JSONArray texts = JSONArray.parseArray(JSONObject.parseObject(jsonString.get("data").toString()).get("texts").toString());
            JSONObject jsonObject = JSONObject.parseObject(str);
            List<String> list = new ArrayList<>();
            int rarityMax = 3;
            int rarityMin = 2;
            for (Object text : texts) {
                String textStr = (String) JSONObject.parseObject(text.toString()).get("text");
                if (jsonObject.get(textStr) != null) {
                    if ((textStr).startsWith("高级")) {
                        list.add(0, jsonObject.get(textStr).toString());
                        rarityMax = 5;
                    } else {

                        list.add(textStr);
                    }
                }
            }

            List<HashMap<Object, Object>> groupMessage = new ArrayList<>();


            log.info(JSON.toJSONString(list));
            if (list.size() == 5) {
                String message = recruitCal(list, rarityMax);
//                System.out.println(message);
                HashMap<Object, Object> messageMap = getMessageMap(message, false);
                groupMessage.add(messageMap);
                sendGroupMessage(group_id, JSON.toJSONString(groupMessage));
                log.info("发送成功:==>{}", "公招");
                return true;
            }

            if (list.size() > 2 && list.size() < 5) {
                StringBuilder tagText = new StringBuilder();
                for (String tag : list) {
                    tagText.append("，").append(tag);
                }
                sendMessage(group_id, "识别失败，仅识别了：" + tagText + "，请重新截图");
                return true;
            }
        }
        return false;
    }

    public String recruitCal(List<String> tags, Integer rarityMax) {
        List<RecruitData> recruitDataList = JSONArray.parseArray(FileUtil.read(FileConfig.Bot + "tag_char.json"), RecruitData.class);
//        List<String> tags = Arrays.asList("高级资深干员", "近战位", "控场", "资深干员", "防护");

        HashMap<String, List<RecruitData>> recruitResultMap = new HashMap<>();
//        System.out.println(tags);
        assert recruitDataList != null;
        recruitDataList.stream().filter(recruitData -> recruitData.getRarity() <= 7).forEach(recruitData -> {
            List<String> conformingTag = new ArrayList<>();
            for (String tag : tags) {
                if (tag.equals(recruitData.getLevel())) conformingTag.add(tag);
                if (tag.equals(recruitData.getProfession())) conformingTag.add(tag);
                if (tag.equals(recruitData.getPosition())) conformingTag.add(tag);
                recruitData.getTags().stream().filter(t -> t.equals(tag)).forEach(conformingTag::add);
            }
//            System.out.println(recruitData.getName()+"——的tag是："+conformingTag);
            if (conformingTag.size() > 0) {
                for (int i = 0; i < conformingTag.size(); i++) {
                    String selectOneTag = conformingTag.get(i);
                    saveRecruitResult(recruitResultMap, selectOneTag, recruitData);
                    for (int j = i + 1; j < conformingTag.size(); j++) {
                        String selectTwoTag = conformingTag.get(i) + "+" + conformingTag.get(j);
                        saveRecruitResult(recruitResultMap, selectTwoTag, recruitData);
                        for (int k = j + 1; k < conformingTag.size(); k++) {
                            String selectThreeTag = conformingTag.get(i) + "+" + conformingTag.get(j) + "+" + conformingTag.get(k);
                            saveRecruitResult(recruitResultMap, selectThreeTag, recruitData);
                        }
                    }
                }
            }
        });


        StringBuilder result = new StringBuilder(" ");

        List<RecruitResultVo> recruitResultVoList = new ArrayList<>();


        for (String key : recruitResultMap.keySet()) {
            List<RecruitData> list = recruitResultMap.get(key);
            list.sort(Comparator.comparing(RecruitData::getRarity));
            if (list.get(0).getRarity() > 3)
               recruitResultVoList.add(new RecruitResultVo(list.get(0).getRarity(), list, key));

        }

        recruitResultVoList.sort(Comparator.comparing(RecruitResultVo::getRarityMax).reversed());

        for (RecruitResultVo recruitResultVo : recruitResultVoList) {
//            System.out.println(recruitResultVo.getTag());
            StringBuilder charNamesText = new StringBuilder();
            if (recruitResultVo.getRarityMax() > rarityMax) {
                for (RecruitData recruitData : recruitResultVo.getRecruitDataList()) {
                    charNamesText.append(recruitData.getName()).append("   ");
                }
                result.append("【").append(recruitResultVo.getTag()).append("】:\n").append(charNamesText).append("\n\n");
            }
        }

//        if (list.get(0).getRarity()>rarityMax) {
//            StringBuilder charNamesText = new StringBuilder();
//            for(RecruitData recruitData:list){
//                charNamesText.append(recruitData.getName()).append("   ");
//            }
//            result.append("【").append(key).append("】:\n").append(charNamesText).append("\n\n");
//        };


        return result.toString();
    }

    private void saveRecruitResult(HashMap<String, List<RecruitData>> recruitResult, String tag, RecruitData recruitData) {

        if (recruitData.getRarity() == 6 && !tag.contains("高级")) return;
        if (recruitData.getRarity() < 3) return;
//        System.out.println(tag+"："+recruitData.getName());
        if (recruitResult.get(tag) == null) {
            List<RecruitData> recruitDataList = new ArrayList<>();
            recruitDataList.add(recruitData);
            recruitResult.put(tag, recruitDataList);
        } else {
            List<RecruitData> recruitDataList = recruitResult.get(tag);
            recruitDataList.add(recruitData);
            recruitResult.put(tag, recruitDataList);
        }
    }



    public  String checkPenguinData(){

        String penguin_url = "https://penguin-stats.io/PenguinStats/api/v2/result/matrix?show_closed_zones=true";
        String stage_url = "https://backend.yituliu.site/stage";
        String penguin_response = HttpRequestUtil.doGet(penguin_url,new HashMap<>());
        String stage_response = HttpRequestUtil.doGet(stage_url,new HashMap<>());
        List<PenguinDataResponseVo> newData = JSONArray.parseArray(JSONObject.parseObject(penguin_response).getString("matrix"), PenguinDataResponseVo.class);
        JSONArray stageData = JSONArray.parseArray(JSONObject.parseObject(stage_response).getString("data"));
        HashMap<String, String> stageMap = new HashMap<>();
        stageData.forEach(obj->{
            JSONObject jsonObject = JSONObject.parseObject(String.valueOf(obj));
            stageMap.put(jsonObject.getString("stageId"),jsonObject.getString("stageCode"));
        });


        System.out.println(FileConfig.Check + "matrix global.json");
        List<PenguinDataResponseVo> oldData = JSONArray.parseArray(JSONObject.parseObject(FileUtil.read(FileConfig.Check + "matrix global.json")).getString("matrix"), PenguinDataResponseVo.class);
        List<ItemVo> ItemList = JSONArray.parseArray(JSONObject.parseObject(FileUtil.read(FileConfig.Check + "item.json")).getString("data"), ItemVo.class);
        Map<String, ItemVo> itemMap = ItemList.stream().collect(Collectors.toMap(ItemVo::getItemId, Function.identity()));

        HashMap<String, Double> oldDataMap = new HashMap<>();

        oldData.forEach(penguin->oldDataMap.put(penguin.getStageId()+"_"+penguin.getItemId(),(double)penguin.getQuantity()/(double)penguin.getTimes()*100));
        StringBuilder message = new StringBuilder();

        for(PenguinDataResponseVo penguin:newData){
            double newDataProbability = (double)penguin.getQuantity()/(double)penguin.getTimes()*100;
            if(oldDataMap.get(penguin.getStageId()+"_"+penguin.getItemId())==null) continue;
            if(itemMap.get(penguin.getItemId())==null) continue;
            if(stageMap.get(penguin.getStageId())==null) continue;
            if(penguin.getTimes()<300) continue;
            ItemVo itemVo = itemMap.get(penguin.getItemId());
            double oldDataProbability  = oldDataMap.get(penguin.getStageId()+"_"+penguin.getItemId());
            double difference = newDataProbability - oldDataProbability;
            double absolute  =  difference>0?difference:-difference;

            if( itemVo.getRarity()==4){     //判断是紫色品质
                if(oldDataProbability<10&&absolute>0.4)   //掉率<10%的±0.3%记录
                    messageAppend(message,stageMap.get(penguin.getStageId()),itemVo.getItemName(),absolute,difference);
                if(oldDataProbability>=10&&absolute>0.8)  //掉率>10%的±0.5%记录
                    messageAppend(message,stageMap.get(penguin.getStageId()),itemVo.getItemName(),absolute,difference);
            }

            if( itemVo.getRarity()==3){   //判断是蓝色品质
                if(oldDataProbability<20&&absolute>0.5)  //掉率<20%的±0.5%记录
                    messageAppend(message,stageMap.get(penguin.getStageId()),itemVo.getItemName(),absolute,difference);
                if(oldDataProbability>=20&&absolute>1)   //掉率>20%的±1%记录
                    messageAppend(message,stageMap.get(penguin.getStageId()),itemVo.getItemName(),absolute,difference);
            }


        }
        System.out.println(message);
        String messageStr = String.valueOf(message);
        if(messageStr.length()>3000) messageStr = "超出单条消息长度";
        if(messageStr.length()<5) messageStr = "无事发生";
        List<HashMap<Object, Object>> groupMessage = new ArrayList<>();  //QQ转发消息数组
        HashMap<Object, Object> messageMap = getMessageMap(messageStr, false);
        groupMessage.add(messageMap);
        sendGroupMessage(189844877L, JSON.toJSONString(groupMessage));

        return messageStr;
    }

    private static void messageAppend(StringBuilder message, String stageId,String itemName,Double absolute,Double difference){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
         message.append(stageId).append("的[").append(itemName)
                        .append("]\n掉率").append(difference>0?("+"+decimalFormat.format(difference)):decimalFormat.format(difference)).append("%\n\n");
    }

    public String checkItemData() {
        String item_url = "https://backend.yituliu.site/item/value?expCoefficient=0.625";
        String item_response = HttpRequestUtil.doGet(item_url,new HashMap<>());
        List<ItemVo> newItemList = JSONArray.parseArray(JSONObject.parseObject(item_response).getString("data"), ItemVo.class);
        Map<String, Double> collect = newItemList.stream().collect(Collectors.toMap(ItemVo::getItemId, ItemVo::getItemValueAp));


        List<ItemVo> itemList = JSONArray.parseArray(JSONObject.parseObject(FileUtil.read(FileConfig.Check + "item.json")).getString("data"), ItemVo.class);
        StringBuilder message = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        for(ItemVo itemVo:itemList){
            if(collect.get(itemVo.getItemId())==null)continue;
            Double newItemValueAp = collect.get(itemVo.getItemId());
            if(newItemValueAp==0) continue;
            Double itemValueAp = itemVo.getItemValueAp();

            double absolute = (1-(itemValueAp/newItemValueAp))>0?(1-(itemValueAp/newItemValueAp)):-(1-(itemValueAp/newItemValueAp));

            if(absolute>0.05) message.append(itemVo.getItemName()).append("浮动").append(decimalFormat.format(absolute)).append("%\n");

        }
        System.out.println(message);
      return String.valueOf(message);
    }







    private static HashMap<Object, Object> getMessageMap(String message, boolean messageType) {
        HashMap<Object, Object> messageMap = new HashMap<>();
        HashMap<Object, Object> content = new HashMap<>();
        content.put("name", "桜");
        content.put("uin", "1820702789");
        if (messageType) {
            content.put("content", message);
        } else {
            HashMap<Object, Object> text = new HashMap<>();
            text.put("text", message);
            HashMap<Object, Object> textMap = new HashMap<>();
            textMap.put("type", "text");
            textMap.put("data", text);
            List<HashMap<Object, Object>> list = new ArrayList<>();
            list.add(textMap);
            content.put("content", list);
        }

        messageMap.put("type", "node");
        messageMap.put("data", content);
        return messageMap;
    }

    public void sendGroupMessage(long group_id, String message) {
        String str = null;

        try {
            str = URLEncoder.encode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "http://" + FileConfig.CqHttpIp + ":5700/send_group_forward_msg?group_id=" + group_id + "&messages=" +
                str;
        String result = HttpRequestUtil.doGet(url, new HashMap<>());
        log.info("发送成功:==>{}", result);
    }

    public void sendMessage(long group_id, String message) {
        String str = null;
        try {
            str = URLEncoder.encode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = "http://" + FileConfig.CqHttpIp + ":5700/send_group_msg?group_id=" + group_id + "&message=" +
                str;
        String result = HttpRequestUtil.doGet(url, new HashMap<>());
        log.info("发送成功:==>{}", result);
    }

    public void sendImage(String url) {
        String result = HttpRequestUtil.doGet(url, new HashMap<>());
        log.info("发送成功:==>{}", result);
    }
}
