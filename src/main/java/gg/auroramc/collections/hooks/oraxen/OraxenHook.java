package gg.auroramc.collections.hooks.oraxen;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.collections.AuroraCollections;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class OraxenHook implements Hook<AuroraCollections>, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;

        if(Bukkit.getPluginManager().getPlugin("Oraxen").getDescription().getVersion().startsWith("2")) {
            new Oraxen2Hook().hook(plugin);
        }
    }
}
