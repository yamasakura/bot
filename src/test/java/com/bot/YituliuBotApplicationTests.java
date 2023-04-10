package com.bot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bot.common.config.FileConfig;
import com.bot.common.util.FileUtil;
import com.bot.service.vo.ItemVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class YituliuBotApplicationTests {

    @Test
    void contextLoads() {
        List<ItemVo> ItemList = JSONArray.parseArray(FileUtil.read(FileConfig.Check + "item.json"), ItemVo.class);

        List<Object> list = new ArrayList<>();
        for(ItemVo itemVo:ItemList){
            HashMap<Object, Object> hashMap = new HashMap<>();
            hashMap.put("itemId",itemVo.getItemId());
            hashMap.put("itemName",itemVo.getItemName());
            hashMap.put("rarity",itemVo.getRarity());
            list.add(hashMap);
        }

      FileUtil.save(FileConfig.Check,"item.json", JSON.toJSONString(list));
    }

}
