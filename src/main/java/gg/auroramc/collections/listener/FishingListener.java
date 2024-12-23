package gg.auroramc.collections.listener;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;


public class FishingListener implements Listener {
    private final AuroraCollections plugin;

    public FishingListener(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (e.getCaught() == null) return;
        if (!(e.getCaught() instanceof Item item)) return;

        var type = item.getItemStack().getType();
        var amount = item.getItemStack().getAmount();

        var manager = plugin.getCollectionManager();
        manager.progressCollections(e.getPlayer(), TypeId.from(type), amount, Trigger.FISH);
    }
}
