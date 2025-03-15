package gg.auroramc.collections.hooks;

import gg.auroramc.collections.hooks.auraskills.AuraSkillsHook;
import gg.auroramc.collections.hooks.auroralevels.AuroraLevelsHook;
import gg.auroramc.collections.hooks.beeminions.BeeMinionsHook;
import gg.auroramc.collections.hooks.customfishing.CustomFishingHook;
import gg.auroramc.collections.hooks.luckperms.LuckPermsHook;
import gg.auroramc.collections.hooks.mmocore.MMOCoreHook;
import gg.auroramc.collections.hooks.mmoitems.MMOItemsHook;
import gg.auroramc.collections.hooks.mmolib.MMOLibHook;
import gg.auroramc.collections.hooks.mythic.MythicHook;
import gg.auroramc.collections.hooks.nexo.NexoHook;
import gg.auroramc.collections.hooks.oraxen.OraxenHook;
import gg.auroramc.collections.hooks.topminions.TopMinionsHook;
import gg.auroramc.collections.hooks.worldguard.WorldGuardHook;
import lombok.Getter;

@Getter
public enum Hooks {
    AURORA_LEVELS(AuroraLevelsHook.class, "AuroraLevels"),
    AURA_SKILLS(AuraSkillsHook.class, "AuraSkills"),
    LUCK_PERMS(LuckPermsHook.class, "LuckPerms"),
    CUSTOM_FISHING(CustomFishingHook.class, "CustomFishing"),
    MMOITEMS(MMOItemsHook.class, "MMOItems"),
    MYTHIC_MOBS(MythicHook.class, "MythicMobs"),
    ORAXEN(OraxenHook.class, "Oraxen"),
    NEXO(NexoHook.class, "Nexo"),
    WORLD_GUARD(WorldGuardHook.class, "WorldGuard"),
    MMOLIB(MMOLibHook.class, "MythicLib"),
    TOP_MINIONS(TopMinionsHook.class, "TopMinion"),
    BEE_MINIONS(BeeMinionsHook.class, "BeeMinions"),
    MMOCORE(MMOCoreHook.class, "MMOCore");

    private final Class<? extends Hook> clazz;
    private final String plugin;

    Hooks(Class<? extends Hook> clazz, String plugin) {
        this.clazz = clazz;
        this.plugin = plugin;
    }
}
