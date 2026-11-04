package gg.auroramc.collections.hooks.oraxen;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import io.th0rgal.oraxen.api.events.custom_block.noteblock.OraxenNoteBlockDropLootEvent;
import io.th0rgal.oraxen.api.events.custom_block.stringblock.OraxenStringBlockDropLootEvent;
import io.th0rgal.oraxen.utils.drops.DroppedLoot;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class Oraxen2Hook implements Hook<AuroraCollections>, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        AuroraCollections.logger().info("Hooked into Oraxen 2 for collection progression with namespace 'oraxen'.");
    }

    @EventHandler
    public void onNoteBlockLootDrop(OraxenNoteBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleProgression(e.getPlayer(), e.getLoots());
    }

    @EventHandler
    public void onStringBlockLootDrop(OraxenStringBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleProgression(e.getPlayer(), e.getLoots());
    }

    private boolean invalid(Player player, Block block) {
        return player == null || block == null || AuroraAPI.getRegionManager().isPlacedBlock(block);
    }

    private void handleProgression(Player player, List<DroppedLoot> droppedLootList) {
        for (DroppedLoot droppedLoot : droppedLootList) {
            var itemStack = droppedLoot.loot().itemStack();
            var typeId = plugin.getItemManager().resolveId(itemStack);
            plugin.getCollectionManager().progressCollections(player, typeId, droppedLoot.amount(), Trigger.BLOCK_LOOT);
        }
    }
}
