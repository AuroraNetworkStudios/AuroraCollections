package gg.auroramc.collections.hooks.mmoitems;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.ItemDropEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MMOItemsHook implements Hook<AuroraCollections>, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;
        AuroraCollections.logger().info("Hooked into MMOItems for block loot collection with namespace 'mmoitems'.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockBreak(ItemDropEvent event) {
        if (event.getCause() != ItemDropEvent.DropCause.CUSTOM_BLOCK) return;
        if (!(event.getWhoDropped() instanceof Player player)) return;
        if (AuroraAPI.getRegionManager().isPlacedBlock(event.getMinedBlock())) return;

        for (var drop : event.getDrops()) {
            TypeId typeId;
            var nbtItem = NBTItem.get(drop);
            if(nbtItem.hasType()) {
                typeId = new TypeId("mmoitems", nbtItem.getType() + ":" + nbtItem.getString("MMOITEMS_ITEM_ID"));
            } else {
                typeId = TypeId.from(drop.getType());
            }

            plugin.getCollectionManager().progressCollections(player, typeId, drop.getAmount(), Trigger.BLOCK_LOOT);
        }
    }
}
