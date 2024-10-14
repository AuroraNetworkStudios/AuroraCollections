package gg.auroramc.collections.listener;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.CollectionManager;
import gg.auroramc.collections.collection.Trigger;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VillagerTradeListener implements Listener {
    private final AuroraCollections plugin;

    public VillagerTradeListener(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrade(PlayerPurchaseEvent event) {

        Player player = event.getPlayer();
        TypeId typeId = AuroraAPI.getItemManager().resolveId(event.getTrade().getResult());
        int amount = event.getTrade().getResult().getAmount();

        CollectionManager manager = plugin.getCollectionManager();
        manager.progressCollections(player, typeId, amount, Trigger.VILLAGER_TRADE);
    }
}
