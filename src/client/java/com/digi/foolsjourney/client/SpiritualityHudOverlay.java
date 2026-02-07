package com.digi.foolsjourney.client;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class SpiritualityHudOverlay implements HudRenderCallback {

    private long lastActiveTime = 0;

    private static final long WAIT_TIME = 1000;
    private static final long FADE_TIME = 1000;

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.player.isSpectator()) return;

        if (client.player instanceof IBeyonder beyonder) {
            if (beyonder.getSequence() == -1) return;

            double currentMana = beyonder.getSpirituality();
            double maxMana = 100.0;

            if (currentMana < maxMana) {
                lastActiveTime = System.currentTimeMillis();
            }

            long timeSince = System.currentTimeMillis() - lastActiveTime;
            float alpha = 1.0f;

            if (currentMana >= maxMana) {
                if (timeSince > (WAIT_TIME + FADE_TIME)) {
                    return;
                }
                else if (timeSince > WAIT_TIME) {
                    float fadeProgress = (float) (timeSince - WAIT_TIME) / FADE_TIME;
                    alpha = 1.0f - fadeProgress;

                    if (alpha < 0) alpha = 0;
                }
            }

            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();

            int barWidth = 102;
            int barHeight = 6;
            int x = 10;
            int y = height - 55;

            float percentage = (float) (currentMana / maxMana);
            int filledWidth = (int) (100 * percentage);

            int frameColor = getColorWithAlpha(0x000000, 0.5f * alpha);

            int barColor = getColorWithAlpha(0x9B30FF, alpha);

            context.fill(x, y, x + barWidth, y + barHeight, frameColor);

            if (filledWidth > 0) {
                context.fill(x + 1, y + 1, x + 1 + filledWidth, y + barHeight - 1, barColor);
            }
        }
    }
    private int getColorWithAlpha(int color, float alpha) {
        int alphaInt = (int) (alpha * 255);
        if (alphaInt < 0) alphaInt = 0;
        if (alphaInt > 255) alphaInt = 255;

        return (alphaInt << 24) | (color & 0x00FFFFFF);
    }
}