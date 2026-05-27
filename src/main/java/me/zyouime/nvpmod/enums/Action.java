package me.zyouime.nvpmod.enums;

public enum Action {
    BAN("Забаненных по проверкам"),
    NVP_BAN("Банов по NVP"),
    FREEZE("Кол-во заморозок"),
    UNBAN("Разбаненных"),
    UNFREEZE("Кол-во разморозок");

    public final String desc;
    Action(String desc) {
        this.desc = desc;
    }
}
