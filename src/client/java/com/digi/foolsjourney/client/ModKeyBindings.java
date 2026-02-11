package com.digi.foolsjourney.client;

import com.digi.foolsjourney.networking.ModMessages;
import com.digi.foolsjourney.networking.SpiritVisionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static KeyBinding SPIRIT_VISION_KEY;
    public static KeyBinding FLAME_SNAP_KEY;
    public static KeyBinding AIR_BULLET_KEY;

    public static void register() {
        SPIRIT_VISION_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.foolsjourney.spirit_vision",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.foolsjourney.lotm"
        ));

        FLAME_SNAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.foolsjourney.flame_snap",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.foolsjourney.lotm"
        ));

        AIR_BULLET_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.foolsjourney.air_bullet",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.foolsjourney.lotm"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SPIRIT_VISION_KEY.wasPressed()) {
                if (ClientPlayNetworking.canSend(SpiritVisionPayload.ID)) {
                    ClientPlayNetworking.send(new SpiritVisionPayload());
                }
            }

            while (FLAME_SNAP_KEY.wasPressed()) {
                if (ClientPlayNetworking.canSend(ModMessages.FlameSnapPayload.ID)) {
                    ClientPlayNetworking.send(new ModMessages.FlameSnapPayload());
                }
            }

            while (AIR_BULLET_KEY.wasPressed()) {
                if (ClientPlayNetworking.canSend(ModMessages.AirBulletPayload.ID)) {
                    ClientPlayNetworking.send(new ModMessages.AirBulletPayload());
                }
            }
        });
    }
}