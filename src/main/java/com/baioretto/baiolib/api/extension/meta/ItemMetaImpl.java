package com.baioretto.baiolib.api.extension.meta;

import com.baioretto.baiolib.api.Pool;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * The {@code ItemMetaImpl} class used with {@link lombok.experimental.ExtensionMethod}
 *
 * @author baioretto
 * @since 1.1.0
 */
public class ItemMetaImpl {
    /**
     * Set meta display name.
     *
     * @param in          the {@link ItemMeta} instance
     * @param displayName the display name {@link Component}
     */
    public static void displayName(ItemMeta in, Component displayName) {
        Pool.get(PaperItemMeta.class).impl().displayName(in, displayName);
    }

    /**
     * Set meta lore list.
     *
     * @param in   the {@link ItemMeta} instance
     * @param lore the lore list {@link Component}
     */
    public static void lore(ItemMeta in, List<Component> lore) {
        Pool.get(PaperItemMeta.class).impl().lore(in, lore);
    }
}
