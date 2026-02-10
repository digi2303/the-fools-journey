package com.digi.foolsjourney.command;

import com.digi.foolsjourney.util.IBeyonder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModCommands {

    private static final SuggestionProvider<ServerCommandSource> SEQUENCE_SUGGESTIONS = (context, builder) -> {
        builder.suggest(9, Text.translatable("command.suggestion.foolsjourney.seer"));
        builder.suggest(8, Text.translatable("command.suggestion.foolsjourney.clown"));
        builder.suggest(7, Text.translatable("command.suggestion.foolsjourney.magician"));
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("lotm")
                .requires(source -> source.hasPermissionLevel(2))

                .then(CommandManager.literal("status")
                        .executes(ModCommands::runStatusSelf)
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ModCommands::runStatusTarget)))

                .then(CommandManager.literal("set")
                        .then(CommandManager.literal("sequence")
                                .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 9))
                                        .suggests(SEQUENCE_SUGGESTIONS)
                                        .executes(ModCommands::runSetSequenceSelf)
                                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                                .executes(ModCommands::runSetSequenceTarget))))

                        .then(CommandManager.literal("digestion")
                                .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.0, 100.0))
                                        .executes(ModCommands::runSetDigestionSelf)
                                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                                .executes(ModCommands::runSetDigestionTarget))))

                        .then(CommandManager.literal("spirituality")
                                .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                        .executes(ModCommands::runSetSpiritualitySelf)
                                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                                .executes(ModCommands::runSetSpiritualityTarget))))
                )

                .then(CommandManager.literal("reset")
                        .executes(ModCommands::runResetSelf)
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ModCommands::runResetTarget)))
        );
    }
    private static int statusLogic(ServerCommandSource source, ServerPlayerEntity target) {
        if (target instanceof IBeyonder beyonder) {
            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.status_header", target.getName().getString()).formatted(Formatting.GOLD), false);

            int seq = beyonder.getSequence();
            String seqText = (seq == -1) ? "None" : String.valueOf(seq);

            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.status_sequence", seqText).formatted(Formatting.YELLOW), false);
            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.status_spirituality", String.format("%.1f", beyonder.getSpirituality())).formatted(Formatting.AQUA), false);

            String digestionStr = String.format("%.1f", beyonder.getDigestion());
            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.status_digestion", digestionStr).formatted(Formatting.GREEN), false);
        }
        return 1;
    }

    private static int setSequenceLogic(ServerCommandSource source, ServerPlayerEntity target, int level) {
        if (target instanceof IBeyonder beyonder) {
            beyonder.setSequence(level);

            double maxSpirituality = 100.0;
            if (level == 7) {
                maxSpirituality = 400.0;
            } else if (level == 8) {
                maxSpirituality = 200.0;
            }

            beyonder.setSpirituality(maxSpirituality);
            beyonder.setDigestion(0.0);

            beyonder.syncBeyonderData();

            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.set_sequence", target.getName().getString(), level).formatted(Formatting.GREEN), true);
        }
        return 1;
    }

    private static int setDigestionLogic(ServerCommandSource source, ServerPlayerEntity target, double amount) {
        if (target instanceof IBeyonder beyonder) {
            beyonder.setDigestion(amount);
            beyonder.syncBeyonderData();

            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.set_digestion", target.getName().getString(), String.format("%.1f", amount)).formatted(Formatting.GREEN), true);
        }
        return 1;
    }

    private static int setSpiritualityLogic(ServerCommandSource source, ServerPlayerEntity target, double amount) {
        if (target instanceof IBeyonder beyonder) {
            beyonder.setSpirituality(amount);
            beyonder.syncBeyonderData();

            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.set_spirituality", target.getName().getString(), String.format("%.1f", amount)).formatted(Formatting.GREEN), true);
        }
        return 1;
    }

    private static int resetLogic(ServerCommandSource source, ServerPlayerEntity target) {
        if (target instanceof IBeyonder beyonder) {
            beyonder.setSequence(-1);
            beyonder.setSpirituality(0);
            beyonder.setDigestion(0);
            beyonder.setSpiritVision(false);
            beyonder.setCooldown(0);

            beyonder.syncBeyonderData();

            source.sendFeedback(() -> Text.translatable("commands.foolsjourney.reset_success", target.getName().getString()).formatted(Formatting.RED), true);
        }
        return 1;
    }
    private static int runStatusSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return statusLogic(context.getSource(), context.getSource().getPlayerOrThrow());
    }
    private static int runStatusTarget(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return statusLogic(context.getSource(), EntityArgumentType.getPlayer(context, "target"));
    }

    private static int runSetSequenceSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return setSequenceLogic(context.getSource(), context.getSource().getPlayerOrThrow(), IntegerArgumentType.getInteger(context, "level"));
    }
    private static int runSetSequenceTarget(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return setSequenceLogic(context.getSource(), EntityArgumentType.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "level"));
    }

    private static int runSetDigestionSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return setDigestionLogic(context.getSource(), context.getSource().getPlayerOrThrow(), DoubleArgumentType.getDouble(context, "amount"));
    }
    private static int runSetDigestionTarget(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return setDigestionLogic(context.getSource(), EntityArgumentType.getPlayer(context, "target"), DoubleArgumentType.getDouble(context, "amount"));
    }

    private static int runSetSpiritualitySelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return setSpiritualityLogic(context.getSource(), context.getSource().getPlayerOrThrow(), DoubleArgumentType.getDouble(context, "amount"));
    }
    private static int runSetSpiritualityTarget(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return setSpiritualityLogic(context.getSource(), EntityArgumentType.getPlayer(context, "target"), DoubleArgumentType.getDouble(context, "amount"));
    }

    private static int runResetSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return resetLogic(context.getSource(), context.getSource().getPlayerOrThrow());
    }
    private static int runResetTarget(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return resetLogic(context.getSource(), EntityArgumentType.getPlayer(context, "target"));
    }
}