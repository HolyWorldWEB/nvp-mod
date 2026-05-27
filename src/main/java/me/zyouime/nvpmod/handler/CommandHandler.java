package me.zyouime.nvpmod.handler;

import me.zyouime.nvpmod.Main;
import me.zyouime.nvpmod.config.ModConfig;
import me.zyouime.nvpmod.enums.Action;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public record CommandHandler(ModConfig config) {

    public void handleCommand(Action action, String triggerPlayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!config.enabled) {
            client.player.sendMessage(Text.literal(Main.PREFIX + "§eМод не включен!"), true);
            return;
        }
        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo == null || !serverInfo.address.toLowerCase().contains("holyworld")) {
            Main.sendMsg("Ты не подключен к HolyWorld!");
            return;
        }
        sendAction(action, triggerPlayer);
    }

    private void sendAction(Action action, String triggerPlayer) {
        Main.http().sendAction(action, triggerPlayer);
    }
}
