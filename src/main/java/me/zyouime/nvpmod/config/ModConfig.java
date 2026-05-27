package me.zyouime.nvpmod.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "nvpmod")
public class ModConfig implements ConfigData {

    public String username = "";
    public String password = "";
    public boolean enabled = true;
    public String vk = "";
}
