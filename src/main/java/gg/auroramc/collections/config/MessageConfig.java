package gg.auroramc.collections.config;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.function.Consumer;

@Getter
public class MessageConfig {
    private String reloaded = "&aReloaded configuration!";
    private String dataNotLoadedYet = "&cData for this player hasn't loaded yet, try again later!";
    private String dataNotLoadedYetSelf = "&cYour data isn't loaded yet, please try again later!";
    private String playerOnlyCommand = "&cThis command can only be executed by a player!";
    private String noPermission = "&cYou don't have permission to execute this command!";
    private String invalidSyntax = "&cInvalid command syntax!";
    private String mustBeNumber = "&cArgument must be a number!";
    private String playerNotFound = "&cPlayer not found!";
    private String commandError = "&cAn error occurred while executing this command!";
    private String menuOpened = "&aOpened collection menu for {player}";
    private String categoryNotFound = "&cCategory {category} not found!";
    private String collectionNotFound = "&cCollection {collection} not found!";
    private String addSuccess = "&aAdded &2{number} &ato &2{collection} &afor &2{player}!";
    private String setSuccess = "&aSet &2{collection} &ato &2{number} &afor &2{player}!";
    private String removeSuccess = "&aRemoved &2{number} &afrom &2{collection} &afor &2{player}!";
    private String resetSuccess = "&aReset &2{collection} &afor &2{player}!";

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", null);
                    yaml.set("category-not-found", "&cCategory {category} not found!");
                    yaml.set("collection-not-found", "&cCollection {collection} not found!");
                    yaml.set("add-success", "&aAdded &2{number} &ato &2{collection} &afor &2{player}!");
                    yaml.set("set-success", "&aSet &2{collection} &ato &2{number} &afor &2{player}!");
                    yaml.set("remove-success", "&aRemoved &2{number} &afrom &2{collection} &afor &2{player}!");
                    yaml.set("reset-success", "&aReset &2{collection} &afor &2{player}!");
                    yaml.set("config-version", 1);
                }
        );
    }
}
