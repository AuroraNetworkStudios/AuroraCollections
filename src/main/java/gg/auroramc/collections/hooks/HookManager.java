package gg.auroramc.collections.hooks;

import gg.auroramc.collections.AuroraCollections;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class HookManager {
    private static final Map<Class<? extends Hook>, Hook> hooks = new HashMap<>();

    public static void enableHooks(AuroraCollections plugin) {
        for (var hook : hooks.values()) {
            hook.hook(plugin);
            if (hook instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) hook, plugin);
            }
        }
    }

    public static void loadHooks(AuroraCollections plugin) {
        for (var hook : Hooks.values()) {
            try {
                if (Bukkit.getPluginManager().getPlugin(hook.getPlugin()) != null) {
                    var instance = hook.getClazz().getDeclaredConstructor().newInstance();
                    instance.hookAtStartUp(plugin);
                    hooks.put(hook.getClazz(), instance);
                }
            } catch (Throwable e) {
                AuroraCollections.logger().warning("Failed to hook " + hook.getPlugin() + ": " + e.getMessage());
            }
        }
    }

    public static <T extends Hook> T getHook(Class<T> clazz) {
        return clazz.cast(hooks.get(clazz));
    }

    public static <T extends Hook> boolean isEnabled(Class<T> clazz) {
        return hooks.get(clazz) != null;
    }
}
