package gg.auroramc.collections.listener;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

public class HarvestingListener implements Listener {
    private final AuroraCollections plugin;

    public HarvestingListener(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHarvest(PlayerHarvestBlockEvent e) {
        // This event will only be called for right click harvestable crops
        var harvested = e.getItemsHarvested();

        var manager = plugin.getCollectionManager();
        for (var item : harvested) {
            manager.progressCollections(e.getPlayer(), TypeId.from(item.getType()), item.getAmount(), Trigger.HARVEST);
        }
    }
}

