package gg.auroramc.collections.hooks.auraskills;

import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import dev.aurelium.auraskills.api.event.user.UserLoadEvent;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import gg.auroramc.collections.listener.BlockBreakListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;


public class AuraSkillsListener implements Listener {
    private final AuroraCollections plugin;
    private final AuraSkillsHook hook;

    public AuraSkillsListener(AuroraCollections plugin, AuraSkillsHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExtraDrop(LootDropEvent e) {
        var item = e.getItem();
        var manager = plugin.getCollectionManager();
        var typeId = TypeId.from(item.getType());

        // mushrooms are probably triggered by foraging luck would be from foraging luck
        if (BlockBreakListener.specialCrops.contains(item.getType()) && typeId.namespace().equals("minecraft")) {
            manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.HARVEST);
            return;
        }

        switch (e.getCause()) {
            case FARMING_LUCK, FARMING_OTHER_LOOT ->
                    manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.HARVEST);
            case FISHING_LUCK, TREASURE_HUNTER, EPIC_CATCH, FISHING_OTHER_LOOT ->
                    manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.FISH);
            case FORAGING_LUCK, FORAGING_OTHER_LOOT, MINING_LUCK, EXCAVATION_OTHER_LOOT, LUCKY_SPADES,
                 MINING_OTHER_LOOT, EXCAVATION_LUCK, METAL_DETECTOR ->
                    manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.BLOCK_LOOT);
            case MOB_LOOT_TABLE ->
                    manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.ENTITY_LOOT);
            case LUCK_DOUBLE_DROP ->
                    manager.progressCollections(e.getPlayer(), typeId, item.getAmount(), Trigger.HARVEST, Trigger.FISH, Trigger.BLOCK_LOOT, Trigger.ENTITY_LOOT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUserDataLoad(UserLoadEvent event) {
        var player = Bukkit.getPlayer(event.getUser().getUuid());
        if (player == null) return;
        CompletableFuture.runAsync(() -> hook.getCorrector().correctRewardsWhenLoaded(player, false));
    }
}
