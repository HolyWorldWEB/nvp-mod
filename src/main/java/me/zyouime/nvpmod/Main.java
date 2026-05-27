package me.zyouime.nvpmod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.zyouime.nvpmod.command.NVPCommand;
import me.zyouime.nvpmod.config.ModConfig;
import me.zyouime.nvpmod.handler.CommandHandler;
import me.zyouime.nvpmod.util.HttpUtil;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Main implements ClientModInitializer {

    private static HttpUtil http;
    private static CommandHandler handler;
    public static final String PREFIX = "§6[NVP] §f";

    @Override
    public void onInitializeClient() {
        ConfigHolder<ModConfig> holder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        http = new HttpUtil(holder.getConfig());
        handler = new CommandHandler(holder.getConfig());
        new NVPCommand(holder, handler);
    }

    public static HttpUtil http() {
        return http;
    }

    public static CommandHandler handler() {
        return handler;
    }

    public static void sendMsg(String msg) {
        MinecraftClient.getInstance().player.sendMessage(Text.literal(Main.PREFIX + msg));
    }

    public static void errorMsg(String msg) {
        sendMsg("§c " + msg);
    }
}
