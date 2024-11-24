package gg.auroramc.collections.listener;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.region.RegionBlockBreakEvent;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

import java.util.Set;

public class BlockBreakListener implements Listener {
    private final AuroraCollections plugin;

    private final Set<Material> crops = Set.of(Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.BEETROOTS, Material.COCOA, Material.NETHER_WART);

    private final Set<Material> blockCrops = Set.of(Material.SUGAR_CANE, Material.CACTUS, Material.BAMBOO);

    public static final Set<Material> specialCrops = Set.of(Material.WARPED_FUNGUS, Material.CRIMSON_FUNGUS, Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM, Material.BROWN_MUSHROOM_BLOCK, Material.RED_MUSHROOM_BLOCK, Material.MELON, Material.PUMPKIN);

    public BlockBreakListener(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(RegionBlockBreakEvent e) {
        if (!e.isNatural()) return;
        var player = e.getPlayerWhoBroke();
        var type = e.getBlock().getType();

        var manager = plugin.getCollectionManager();

        if (blockCrops.contains(type)) {
            manager.progressCollections(player, TypeId.from(type), 1, Trigger.HARVEST);
        }
    }

    @EventHandler
    public void onDrop(BlockDropItemEvent e) {
        var manager = plugin.getCollectionManager();
        var player = e.getPlayer();

        // This is handled above
        if (blockCrops.contains(e.getBlockState().getType())) return;

        if (crops.contains(e.getBlockState().getType())) {
            if (e.getBlockState().getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() != ageable.getMaximumAge()) return;

                for (var drop : e.getItems()) {
                    var item = drop.getItemStack();
                    manager.progressCollections(e.getPlayer(), TypeId.from(item.getType()), item.getAmount(), Trigger.HARVEST);
                }
            }
            return;
        }

        if (AuroraAPI.getRegionManager().isPlacedBlock(e.getBlock())) return;

        if (specialCrops.contains(e.getBlockState().getType())) {
            for (var drop : e.getItems()) {
                var item = drop.getItemStack();
                manager.progressCollections(player, TypeId.from(item.getType()), item.getAmount(), Trigger.HARVEST);
            }
            return;
        }

        for (var drop : e.getItems()) {
            var item = drop.getItemStack();
            manager.progressCollections(player, TypeId.from(item.getType()), item.getAmount(), Trigger.BLOCK_LOOT);
        }
    }
}
