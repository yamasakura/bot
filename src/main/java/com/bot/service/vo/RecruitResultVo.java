package com.bot.service.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitResultVo {
    private Integer rarityMax;
    private List<RecruitData> recruitDataList;
    private String tag;
}
