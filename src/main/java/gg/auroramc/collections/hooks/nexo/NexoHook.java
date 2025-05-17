package gg.auroramc.collections.hooks.nexo;

import com.nexomc.nexo.api.events.custom_block.NexoCustomBlockDropLootEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockDropLootEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockDropLootEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockDropLootEvent;
import com.nexomc.nexo.utils.drops.DroppedLoot;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import gg.auroramc.collections.hooks.Hook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class NexoHook implements Hook, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;
        AuroraCollections.logger().info("Hooked into Nexo for custom block drops (note/string block, chorus fruit).");
    }

    private boolean invalid(Player player, Block block) {
        return player == null || block == null || AuroraAPI.getRegionManager().isPlacedBlock(block);
    }

    @EventHandler
    public void onCustomBlockDrop(NexoStringBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleProgression(e.getPlayer(), e.getLoots());
    }

    @EventHandler
    public void onCustomBlockDrop(NexoChorusBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleProgression(e.getPlayer(), e.getLoots());
    }

    @EventHandler
    public void onCustomBlockDrop(NexoNoteBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleProgression(e.getPlayer(), e.getLoots());
    }

    private void handleProgression(Player player, List<DroppedLoot> droppedLootList) {
        for (DroppedLoot droppedLoot : droppedLootList) {
            var itemStack = droppedLoot.loot().itemStack();
            var typeId = plugin.getItemManager().resolveId(itemStack);
            plugin.getCollectionManager().progressCollections(player, typeId, droppedLoot.amount(), Trigger.BLOCK_LOOT);
        }
    }
}
