package gg.auroramc.collections.hooks.worldguard;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.util.FlagManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook implements Hook<AuroraCollections> {
    @Override
    public void hook(AuroraCollections plugin) {
        AuroraCollections.logger().info("Hooked into WorldGuard for flags");
    }

    @Override
    public void hookAtStartUp(AuroraCollections plugin) {
        FlagManager.registerFlags();
    }

    public boolean isBlocked(Player player) {
        return FlagManager.isBlocked(player);
    }

    public boolean isBlocked(Player player, Location location) {
        return FlagManager.isBlocked(player, location);
    }
}
