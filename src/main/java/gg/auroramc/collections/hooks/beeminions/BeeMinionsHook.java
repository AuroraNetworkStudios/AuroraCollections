package gg.auroramc.collections.hooks.beeminions;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.me.leo_s.beeminions.api.events.RemoveItemStorageMinion;

public class BeeMinionsHook implements Hook<AuroraCollections>, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;
        AuroraCollections.logger().info("Hooked into BeeMinions with trigger: minion_loot");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemTakeOut(RemoveItemStorageMinion event) {
        if (event.getItemAffected() == null) return;

        var player = Bukkit.getPlayer(event.getOwner());
        if (player == null) return;

        var id = plugin.getItemManager().resolveId(event.getItemAffected());
        var amount = event.getItemAffected().getAmount();

        plugin.getCollectionManager().progressCollections(player, id, amount, Trigger.MINION_LOOT);
    }
}
