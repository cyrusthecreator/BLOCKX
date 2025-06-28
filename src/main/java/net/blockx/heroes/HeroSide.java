package net.blockx.heroes;

import org.bukkit.ChatColor;

public enum HeroSide {
    RED(ChatColor.RED),
    BLUE(ChatColor.BLUE);

    private final ChatColor displayColor;

    HeroSide(ChatColor displayColor) {
        this.displayColor = displayColor;
    }

    public ChatColor getDisplayColor() {
        return displayColor;
    }
}
