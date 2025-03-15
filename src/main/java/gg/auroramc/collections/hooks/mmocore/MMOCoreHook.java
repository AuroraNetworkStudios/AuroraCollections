package gg.auroramc.collections.hooks.mmocore;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import gg.auroramc.collections.hooks.Hook;
import net.Indyuce.mmocore.api.event.CustomBlockMineEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MMOCoreHook implements Hook, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;
        AuroraCollections.logger().info("Hooked into MMOCore for custom block loot progression.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDrop(CustomBlockMineEvent event) {
        if (event.getBlock() == null) return;
        if (AuroraAPI.getRegionManager().isPlacedBlock(event.getBlock())) return;

        for (var drop : event.getDrops()) {
            var id = AuroraAPI.getItemManager().resolveId(drop);
            plugin.getCollectionManager().progressCollections(event.getPlayer(), id, drop.getAmount(), Trigger.BLOCK_LOOT);
        }
    }
}
