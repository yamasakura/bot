package com.bot.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bot.common.util.FileUtil;
import com.bot.entity.BuildData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReadGameData {

    private final static String arkPackagePath = "E:/Idea_Project/Arknights-Bot-Resource/gamedata/excel/";

    public Map<String,String> getName(){
        String file_character_table = FileUtil.read(arkPackagePath+"character_table.json");
        JSONObject character_table = JSONObject.parseObject(file_character_table);
        Map<String, String> nameMap = new HashMap<>();
        character_table.forEach((k,v)->{
            String name = JSONObject.parseObject(v.toString()).getString("name");
            nameMap.put(k,name);
        });

        return nameMap;
    }

    public void charSkillData(){
        String file_skill_table = FileUtil.read(arkPackagePath+"skill_table.json");
        String file_character_table = FileUtil.read(arkPackagePath+"character_table.json");
        JSONObject skillJson = JSONObject.parseObject(file_skill_table);
        JSONObject characterJson = JSONObject.parseObject(file_character_table);
        List<String> skillNameList = new ArrayList<>(skillJson.keySet());
        List<String> characterNameList = new ArrayList<>(characterJson.keySet());


    }


    private Map<String,Object>  BuildingSkillData(){
        String file_building_data = FileUtil.read(arkPackagePath+"building_data.json");
        Map<String, String> nameMap = getName();


        JSONObject building_data = JSONObject.parseObject(file_building_data);
        JSONObject chars = JSONObject.parseObject(building_data.getString("chars"));
        JSONObject buffs = JSONObject.parseObject(building_data.getString("buffs"));

        Set<String> charIdList = chars.keySet();
        List<BuildData> buildDataList = new ArrayList<>();
        for(String charId:charIdList){
            Object o = chars.get(charId);
            JSONObject charsInfo = JSONObject.parseObject(chars.getString(charId));
            JSONArray buffChar =  JSONArray.parseArray(charsInfo.getString("buffChar"));
            for (Object buffCharString:buffChar){
                JSONArray buffData = JSONArray.parseArray(JSONObject.parseObject(buffCharString.toString()).getString("buffData"));

                for (Object obj : buffData) {
                    BuildData buildData = new BuildData();
                    Object buffId = JSONObject.parseObject(obj.toString()).get("buffId");
                    Object phase = JSONObject.parseObject(JSONObject.parseObject(obj.toString()).get("cond").toString()).get("phase").toString();
                    buildData.setBuffId(buffId.toString());
                    buildData.setPhase("精英" + phase.toString());

                    buildData.setCharId(charId);
                    Map buff = (Map) buffs.get(buffId);
                    Object buffColor = buff.get("buffColor");
                    Object textColor = buff.get("textColor");
                    buildData.setCharName(nameMap.get(charId));
//                    System.out.println(getbuffName(buff.get("buffName").toString(), buffColor, textColor));
                    buildData.setBuffName(getbuffName(buff.get("buffName").toString(), buffColor, textColor));
                    buildData.setRoomType(buff.get("roomType").toString());

                    buildData.setSkillType(skillType(buff.get("roomType").toString(), buff.get("description").toString()));
                    buildData.setSkillTypeSec(skillTypeSec(buff.get("roomType").toString(), buff.get("description").toString()));

//                System.out.print(list.get(i));
                    buildData.setDescription(getDescription(buff.get("description").toString(), buffColor));
                    String s = skillData(buildData.getDescription());
//                    System.out.print(buildData.getCharName());
//                    System.out.println(",返回的值"+s);
                    buildData.setSkillData(Double.parseDouble(skillData(buildData.getDescription())));

                    buildDataList.add(buildData);
                }

            }


        }


        return null;
    }

    private static String getbuffName(String description, Object buffColor, Object textColor) {
        description = "<a style=\"background-color:" + buffColor + ";color:" + textColor + "\">" + description + "</a>";
        return description;
    }

    private static String getDescription(String description, Object buffColor) {
        description = description.replace("</>", "</a>");
        description = description.replace("<@cc.vup>", "<a style=\"color:yellow\">");
        description = description.replace("<@cc.kw>", "<a style=\"color:yellow\">");
        description = description.replace("<@cc.vdown>", "<a style=\"color:yellow\">");
        description = description.replace("<@cc.rem>", "<a style=\"color:yellow\">");

        description = description.replaceAll("<@cc...>|<@cc....>|<@cc.....>|<@cc......>|<@cc.......>|<@cc........>|<@cc.........>|<@cc..........>", "<a >");
        description = description.replaceAll("<[$]cc...>|<[$]cc....>|<[$]cc.....>|<[$]cc......>|<[$]cc.......>|<[$]cc........>|<[$]cc.........>" +
                "|<[$]cc..........>|<[$]cc...........>|<[$]cc............>|<[$]cc.............>", "<a>");
        description = description.replace("</>", "</a>");
//         System.out.println(description);
        return description;
    }

    private static String skillData(String string) {

        try {
//            System.out.println(string);
            int i = string.indexOf("%");
            if (string.indexOf("%") > 0) {
                string = string.substring(string.indexOf("%") - 3, string.indexOf("%"));
                string = string.replace("-", "");
                string = string.replace("+", "");
                string = string.replace(">", "");
                string = string.replace("\"", "");
            } else {
                string = "-22";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "-11";
        }

        return string;
    }


    private static String skillTypeSec(String roomType, String string) {

        if ("TRADING".equals(roomType) && string.contains("订单上限")) return "订单上限" + roomType;
        if ("MANUFACTURE".equals(roomType) && string.contains("仓库容量上限")) return "仓库容量上限" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("先锋干员")) return "先锋干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("狙击干员")) return "狙击干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("医疗干员")) return "医疗干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("术师干员")) return "术师干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("近卫干员")) return "近卫干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("重装干员")) return "重装干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("辅助干员")) return "辅助干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("特种干员")) return "特种干员" + roomType;
        return "没有分类";
    }

    private static String skillType(String roomType, String string) {
        if ("CONTROL".equals(roomType) && string.contains("订单效率")) return "贸易站" + roomType;
        if ("CONTROL".equals(roomType) && string.contains("制造站")) return "制造站" + roomType;
        if ("CONTROL".equals(roomType) && string.contains("心情")) return "心情消耗" + roomType;
        if ("CONTROL".equals(roomType) && string.contains("会客室")) return "会客室" + roomType;
        if ("TRADING".equals(roomType) && string.contains("订单获取效率")) return "订单效率" + roomType;
        if ("TRADING".equals(roomType) && string.contains("订单上限")) return "订单上限" + roomType;
        if ("TRADING".equals(roomType) && string.contains("高品质")) return "高品质" + roomType;
        if ("MANUFACTURE".equals(roomType) && string.contains("贵金属")) return "贵金属" + roomType;
        if ("MANUFACTURE".equals(roomType) && string.contains("仓库容量上限")) return "仓库容量上限" + roomType;
        if ("MANUFACTURE".equals(roomType) && string.contains("作战记录")) return "作战记录" + roomType;
        if ("MANUFACTURE".equals(roomType) && string.contains("源石")) return "源石" + roomType;
        if ("MANUFACTURE".equals(roomType) && string.contains("生产力")) return "通用" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("任意类材料")) return "任意类材料" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("精英材料")) return "精英材料" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("技巧概要")) return "技巧概要" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("基建材料")) return "基建材料" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("炽合金块")) return "炽合金块" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("装置")) return "装置" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("异铁")) return "异铁" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("聚酸酯")) return "聚酸酯" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("源岩")) return "源岩" + roomType;
        if ("WORKSHOP".equals(roomType) && string.contains("芯片")) return "芯片" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("先锋")) return "先锋干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("狙击")) return "狙击干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("医疗")) return "医疗干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("术师")) return "术师干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("近卫")) return "近卫干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("重装")) return "重装干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("辅助")) return "辅助干员" + roomType;
        if ("TRAINING".equals(roomType) && string.contains("特种")) return "特种干员" + roomType;
        return "没有分类";
    }

}
