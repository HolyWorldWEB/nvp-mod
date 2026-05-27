package me.zyouime.nvpmod.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.shedaniel.autoconfig.ConfigHolder;
import me.zyouime.nvpmod.Main;
import me.zyouime.nvpmod.config.ModConfig;
import me.zyouime.nvpmod.enums.Action;
import me.zyouime.nvpmod.handler.CommandHandler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public record NVPCommand(ConfigHolder<ModConfig> configHolder, CommandHandler handler) {

    public NVPCommand(ConfigHolder<ModConfig> configHolder, CommandHandler handler) {
        this.configHolder = configHolder;
        this.handler = handler;
        this.register();
    }

    private void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(ClientCommandManager.literal("client-nvp")
                    .then(ClientCommandManager.literal("auth")
                            .then(ClientCommandManager.argument("auth", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String[] auth = StringArgumentType.getString(context, "auth").split(":");
                                        if (auth.length < 2) {
                                            Main.sendMsg("§cНе верно указаны данные. Правильное использование: §f/nvp auth <login:pass>");
                                            return 1;
                                        }
                                        ModConfig config = configHolder.getConfig();
                                        config.username = auth[0];
                                        config.password = auth[1];
                                        configHolder.save();
                                        successMsg();
                                        Main.http().getProfile();
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("toggle")
                            .executes(context -> {
                                ModConfig config = configHolder.getConfig();
                                config.enabled = !config.enabled;
                                String status = config.enabled ? "§aМод включен" : "§cМод выключен";
                                Main.sendMsg(status);
                                configHolder.save();
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("banip")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.word())
                                    .suggests(onlinePlayers())
                                    .then(ClientCommandManager.argument("time", StringArgumentType.word())
                                            .then(ClientCommandManager.argument("input", StringArgumentType.greedyString())
                                                    .executes(context -> {
                                                        ModConfig config = configHolder.getConfig();
                                                        String input = StringArgumentType.getString(context, "input");
                                                        StringBuilder reasonBuilder = new StringBuilder();
                                                        StringBuilder argsBuilder = new StringBuilder();
                                                        boolean argsMode = false;
                                                        for (String part : input.split(" ")) {
                                                            if (part.startsWith("-")) {
                                                                argsMode = true;
                                                            }
                                                            if (argsMode) {
                                                                argsBuilder.append(part).append(" ");
                                                            } else {
                                                                reasonBuilder.append(part).append(" ");
                                                            }
                                                        }
                                                        String reason = reasonBuilder.toString().trim() + config.vk;
                                                        String arguments = argsBuilder.toString().trim();
                                                        String nickname = StringArgumentType.getString(context, "nickname");
                                                        String time = StringArgumentType.getString(context, "time");
                                                        handler.handleCommand(Action.BAN, nickname);
                                                        sendCommand(context, "banip " + nickname + " " + time + " " + reason + " " + arguments);
                                                        return 1;
                                                    })))))
                    .then(ClientCommandManager.literal("unban")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.greedyString())
                                    .suggests(onlinePlayers())
                                    .executes(context -> {
                                        String nickname = StringArgumentType.getString(context, "nickname");
                                        handler.handleCommand(Action.UNBAN, nickname);
                                        sendCommand(context, "unban " + nickname);
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("freezing")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.greedyString())
                                    .suggests(onlinePlayers())
                                    .executes(context -> freeze(context, Action.FREEZE, StringArgumentType.getString(context, "nickname")))))
                    .then(ClientCommandManager.literal("vk")
                            .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String value = StringArgumentType.getString(context, "value");
                                        ModConfig config = configHolder.getConfig();
                                        config.vk = value;
                                        configHolder.save();
                                        Main.sendMsg("Установлено значение: §7" + config.vk);
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("unfreezing")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.greedyString())
                                    .suggests(onlinePlayers())
                                    .executes(context -> freeze(context, Action.UNFREEZE, StringArgumentType.getString(context, "nickname")))))
                    .then(ClientCommandManager.literal("profile")
                            .executes(context -> {
                                Main.http().getProfile();
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("nvpban")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.greedyString())
                                    .suggests(onlinePlayers())
                                    .executes(context -> {
                                        String nickname = StringArgumentType.getString(context, "nickname");
                                        handler.handleCommand(Action.NVP_BAN, nickname);
                                        sendCommand(context, "nvp ban " + nickname);
                                        return 1;
                                    }))));
            dispatcher.register(ClientCommandManager.literal("cnvp")
                    .redirect(dispatcher.getRoot().getChild("client-nvp")));
        });
    }

    private SuggestionProvider<FabricClientCommandSource> onlinePlayers() {
        return (context, builder) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() == null) {
                return builder.buildFuture();
            }
            String remaining = builder.getRemaining().toLowerCase();
            client.getNetworkHandler()
                    .getPlayerList()
                    .stream()
                    .map(player -> player.getProfile().getName())
                    .filter(name -> name.toLowerCase().startsWith(remaining))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private int freeze(CommandContext<FabricClientCommandSource> context, Action action, String nickname) {
        handler.handleCommand(action, nickname);
        sendCommand(context, "freezing " + nickname);
        return 1;
    }

    private void sendCommand(CommandContext<FabricClientCommandSource> context, String command) {
        context.getSource().getClient().getNetworkHandler().sendCommand(command);
    }

    private void successMsg() {
        Main.sendMsg("Успешно");
    }
}
