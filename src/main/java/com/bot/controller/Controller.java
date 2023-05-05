package com.bot.controller;


import com.bot.common.util.Result;
import com.bot.service.BotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@Api(tags = "内部api")
@RequestMapping(value = "/")
@CrossOrigin()
@Slf4j
public class Controller {

    @Resource
    private BotService botService;

    @ApiOperation(value = "检查企鹅数据")
    @GetMapping("penguin")
    public Result checkPenguinData() {
        String message = botService.checkPenguinData();
        return Result.success(message);
    }

    @ApiOperation(value = "检查物品价值")
    @GetMapping("item")
    public Result checkItemData() {
        String message = botService.checkItemData();
        return Result.success(message);
    }


}
