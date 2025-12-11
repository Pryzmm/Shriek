package com.pryzmm;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import com.pryzmm.client.event.EventHandler;
import java.lang.reflect.Constructor;

public class ShriekClient {
    public static boolean recordingSpeech = false;
    public static boolean alwaysOnRecording = true;
    public static boolean printToChat = false;
    public static boolean printToConsole = false;

    public static KeyMapping vKeyMapping = createKeyMapping();

    private static KeyMapping createKeyMapping() {
        String key = "key.shriek.push_to_mute";
        InputConstants.Type type = InputConstants.Type.KEYSYM;
        int keyCode = InputConstants.KEY_V;

        // 1.21-1.21.8 Keymapping constructor
        try {
            Constructor<KeyMapping> constructor = KeyMapping.class.getConstructor(
                String.class,
                InputConstants.Type.class,
                int.class,
                String.class
            );
            return constructor.newInstance(key, type, keyCode, "category.shriek.shriek");
        }
        catch (NoSuchMethodException ignored) {}
        catch (Exception e) { throw new RuntimeException("Failed to create KeyMapping with String category", e); }

        // 1.21.9+ Keymapping constructor
        try {
            Class<?> categoryClass = null;
            for (Class<?> innerClass : KeyMapping.class.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals("Category") || innerClass.getSimpleName().startsWith("class_")) {
                    categoryClass = innerClass;
                    break;
                }
            }
            if (categoryClass == null) throw new RuntimeException("KeyMapping.Category class not found");
            ResourceLocation categoryId = ResourceLocation.fromNamespaceAndPath(Shriek.MOD_ID, "shriek");
            Object category = categoryClass.getConstructor(ResourceLocation.class).newInstance(categoryId);
            Constructor<KeyMapping> constructor = KeyMapping.class.getConstructor(
                String.class,
                InputConstants.Type.class,
                int.class,
                categoryClass
            );
            return constructor.newInstance(key, type, keyCode, category);
        } catch (Exception ignored) {}

        return null; // Could not create a KeyMapping
    }

    public static void init() {
        KeyMappingRegistry.register(vKeyMapping);
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (alwaysOnRecording) recordingSpeech = !vKeyMapping.isDown();
            else recordingSpeech = vKeyMapping.isDown();
        });

        EventHandler.register();
    }
}