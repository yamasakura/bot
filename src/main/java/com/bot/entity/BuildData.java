package com.bot.entity;

public class BuildData {

    private String charId;
    private String charName;
    private String phase;
    private String buffId;
    private String roomType;
    private String buffName;
    private String description;
    private String skillType;
    private String skillTypeSec;
    private Double skillData;

    public String getCharName() {
        return charName;
    }

    public void setCharName(String charName) {
        this.charName = charName;
    }

    public String getCharId() {
        return charId;
    }

    public void setCharId(String charId) {
        this.charId = charId;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getBuffId() {
        return buffId;
    }

    public void setBuffId(String buffId) {
        this.buffId = buffId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getBuffName() {
        return buffName;
    }

    public void setBuffName(String buffName) {
        this.buffName = buffName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public String getSkillTypeSec() {
        return skillTypeSec;
    }

    public void setSkillTypeSec(String skillTypeSec) {
        this.skillTypeSec = skillTypeSec;
    }

    public Double getSkillData() {
        return skillData;
    }

    public void setSkillData(Double skillData) {
        this.skillData = skillData;
    }

    @Override
    public String toString() {
        return "BuildData{" +
                "phase='" + phase + '\'' +
                ", buffId='" + buffId + '\'' +
                ", roomType='" + roomType + '\'' +
                ", buffName='" + buffName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
