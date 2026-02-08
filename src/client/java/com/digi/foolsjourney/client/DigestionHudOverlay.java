package com.digi.foolsjourney.client;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class DigestionHudOverlay implements HudRenderCallback {

    private long lastActiveTime = 0;
    private static final long WAIT_TIME = 2000;
    private static final long FADE_TIME = 1000;

    private double lastDigestion = -1;

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.player.isSpectator()) return;
        if (client.options.hudHidden) return;

        if (client.player instanceof IBeyonder beyonder) {

            if (beyonder.getSequence() == -1) return;

            double currentDigestion = beyonder.getDigestion();
            double maxDigestion = 100.0;

            if (currentDigestion != lastDigestion) {
                lastActiveTime = System.currentTimeMillis();
                lastDigestion = currentDigestion;
            }

            long timeSince = System.currentTimeMillis() - lastActiveTime;

            if (timeSince > (WAIT_TIME + FADE_TIME)) {
                return;
            }

            float alpha = 1.0f;
            if (timeSince > WAIT_TIME) {
                float fadeProgress = (float) (timeSince - WAIT_TIME) / FADE_TIME;
                alpha = 1.0f - fadeProgress;

                if (alpha < 0) alpha = 0;
            }

            if (alpha <= 0.05f) return;

            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();

            int barWidth = 102;
            int barHeight = 6;

            int x = 10;
            int y = height - 67;

            float percentage = (float) (currentDigestion / maxDigestion);
            if (percentage > 1.0f) percentage = 1.0f;

            int filledWidth = (int) (100 * percentage);

            int frameColor = getColorWithAlpha(0x000000, 0.5f * alpha);
            int barColor = getColorWithAlpha(0x00FFFF, alpha);

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