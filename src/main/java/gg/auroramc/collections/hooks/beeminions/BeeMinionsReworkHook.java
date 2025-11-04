package gg.auroramc.collections.hooks.beeminions;

import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import gg.auroramc.collections.hooks.Hook;
import me.leo_s.beeminions.api.events.MinionItemsRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BeeMinionsReworkHook implements Hook, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;
        AuroraCollections.logger().info("Hooked into BeeMinionsRework with trigger: minion_loot");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemTakeOut(MinionItemsRemoveEvent event) {
        if (event.getResult() != MinionItemsRemoveEvent.ResultState.SUCESS) return;
        if (event.getItems() == null) return;

        var player = Bukkit.getPlayer(event.getMinion().getOwner());
        if (player == null) return;

        for (var item : event.getItems()) {
            var id = plugin.getItemManager().resolveId(item.getItem());
            var amount = item.getAmount();
            plugin.getCollectionManager().progressCollections(player, id, amount, Trigger.MINION_LOOT);
        }
    }
}
