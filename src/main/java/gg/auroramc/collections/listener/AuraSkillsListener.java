package gg.auroramc.collections.listener;

import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import gg.auroramc.collections.collection.TypeId;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class AuraSkillsListener implements Listener {
    private final AuroraCollections plugin;

    public AuraSkillsListener(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExtraDrop(LootDropEvent e) {
        var item = e.getItem();
        var manager = plugin.getCollectionManager();
        var typeId = TypeId.from(item.getType());

        switch (e.getCause()) {
            case FARMING_LUCK -> manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.HARVEST);
            case FISHING_LUCK, TREASURE_HUNTER, EPIC_CATCH, FISHING_OTHER_LOOT ->
                    manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.FISH);
            case FORAGING_LUCK, FORAGING_OTHER_LOOT, MINING_LUCK, EXCAVATION_OTHER_LOOT, LUCKY_SPADES,
                 MINING_OTHER_LOOT, EXCAVATION_LUCK ->
                    manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.BLOCK_LOOT);
            case LUCK_DOUBLE_DROP -> {
                manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.HARVEST, Trigger.FISH, Trigger.BLOCK_LOOT);
            }
        }
    }
}
