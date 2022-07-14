package com.baioretto.baiolib.command;

import com.baioretto.baiolib.api.Pool;
import com.baioretto.baiolib.api.block.placer.BlockPlacer;
import com.baioretto.baiolib.api.extension.meta.ItemMetaImpl;
import com.baioretto.baiolib.api.extension.stack.ItemStackImpl;
import com.baioretto.baiolib.api.player.PlayerImpl;
import com.baioretto.baiolib.api.player.PlayerUtil;
import com.baioretto.baiolib.util.Validate;
import com.google.gson.*;
import de.tr7zw.nbtapi.NBTItem;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;

@Command("test")
@SuppressWarnings("unused")
@ExtensionMethod({ItemStackImpl.class, ItemMetaImpl.class, PlayerImpl.class})
public class MockTest extends CommandBase implements Listener {
    @SubCommand("testDecomponentV2")
    public void testDecomponentV2(CommandSender commandSender) {
        if (!(commandSender instanceof Player player)) return;

        TextComponent textComponent = Component.text("1", NamedTextColor.GREEN)
                .append(Component.text("2").color(TextColor.color(255, 192, 203)).decorate(TextDecoration.BOLD))
                .append(Component.text("3")
                        .append(Component.text("3.1").clickEvent(ClickEvent.openUrl("https://wiki.vg/Chat"))
                                .append(Component.text("3.1.1"))
                                .append(Component.text("3.1.2"))));

        Pool.get(PlayerUtil.class).impl().sendMessage(player, textComponent);
    }

    @SubCommand("testDecomponentV1")
    public void testDecomponentV1(CommandSender commandSender) {
        if (!(commandSender instanceof Player player)) return;
        TextComponent textComponent = Component.text("1", NamedTextColor.DARK_BLUE)
                .append(Component.text("2").color(TextColor.color(255, 192, 203)).decorate(TextDecoration.BOLD))
                .append(Component.text("3")
                        .append(Component.text("3.1").hoverEvent(HoverEvent.showEntity(HoverEvent.ShowEntity.of(Key.key("minecraft:zombie"), UUID.randomUUID())))));

        Pool.get(PlayerUtil.class).impl().sendMessage(player, textComponent);
    }

    @SubCommand("testComponent")
    @SneakyThrows
    public synchronized void testComponent(CommandSender commandSender) {
        if (!(commandSender instanceof Player player)) return;

        ItemStack itemStack = new ItemStack(Material.PAPER);
        itemStack.editMeta(meta -> {
            TextComponent textComponent = Component.text("1", NamedTextColor.DARK_BLUE)
                    .append(Component.text("2").color(TextColor.color(255, 192, 203)).decorate(TextDecoration.BOLD))
                    .append(Component.text("3")
                            .append(Component.text("3.1").hoverEvent(HoverEvent.showEntity(HoverEvent.ShowEntity.of(Key.key("minecraft:zombie"), UUID.randomUUID())))));

            meta.displayName(textComponent);
        });

        System.out.println(new NBTItem(itemStack));

        ItemMeta itemMeta = itemStack.getItemMeta();

        Field field = Class.forName("org.bukkit.craftbukkit.v1_18_R2.inventory.CraftMetaItem").getDeclaredField("displayName");
        field.setAccessible(true);

        Object object = field.get(itemMeta);
        String json = object.toString();

        System.out.printf("json: %s%n", json);

        Component component = GsonComponentSerializer.gson().deserialize(json);

        System.out.printf("textComponent: %s%n", component);

        Pool.get(PlayerUtil.class).impl().sendMessage(player, component);
    }

    private void applyBoolean(JsonObject object, String name, TextComponent.Builder builder) {
        JsonElement element = object.get(name);
        if (Validate.notNull(element) && Validate.matchesBoolean(element.getAsString())) {
            builder.decoration(TextDecoration.valueOf(name.toUpperCase(Locale.ROOT)), element.getAsBoolean());
        }
    }

    @SubCommand("get")
    public void testGetBlock(CommandSender commandSender) {
        if (!(commandSender instanceof CraftPlayer player)) return;

        CraftBlock block = (CraftBlock) player.getTargetBlockExact(5);

        if (block == null) return;
        CraftBlockState craftBlockState = (CraftBlockState) block.getState();

        BlockState blockState = craftBlockState.getHandle();

        blockState.getProperties().forEach(System.out::println);
    }

    @SubCommand("place")
    public void testPlaceBlock(CommandSender commandSender) {
        if (!(commandSender instanceof Player player)) return;
        Pool.get(BlockPlacer.class).impl().placeNoteBlock(player.getWorld(), 10, 1145, -60, 1145);
    }

    @EventHandler
    public void chunk(ChunkLoadEvent event) {
        if (event.getChunk().getX() == 1145 >> 4 && event.getChunk().getZ() == 1145 >> 4) {
            System.out.printf("ChunkLoadEvent: %s\n", event.getChunk());
        }
    }

    @EventHandler
    public void chunk(ChunkUnloadEvent event) {
        if (event.getChunk().getX() == 1145 >> 4 && event.getChunk().getZ() == 1145 >> 4) {
            System.out.printf("ChunkUnloadEvent: %s\n", event.getChunk());
        }
    }
}