package com.baioretto.baiolib.dev;

import com.baioretto.baiolib.BaioLib;
import com.baioretto.baiolib.api.extension.sender.command.ConsoleCommandSenderImpl;
import com.baioretto.baiolib.exception.BaioLibInternalException;
import com.baioretto.baiolib.util.ServerUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;

/**
 * A class that prevent debug time out.
 *
 * @author baioretto
 * @since 1.3.0
 */
@SuppressWarnings("JavaReflectionMemberAccess")
@ExtensionMethod(ConsoleCommandSenderImpl.class)
public final class KeepAliveUpdater extends TimerTask {
    private final AtomicLong timer;
    private final AtomicInteger counter;

    @Getter
    private static boolean isDebugging;

    private KeepAliveUpdater() {
        timer = new AtomicLong(currentTimeMillis());
        counter = new AtomicInteger(0);
    }

    @Override
    public void run() {
        // we don't run below if state not match RUNNABLE
        if (!Thread.State.RUNNABLE.equals(BaioLib.instance().mainThread().getState())) {
            isDebugging = false;
            return;
        }

        // current time - 1s > the latest update time, represent interval more than 1s
        if (currentTimeMillis() - 1100L > timer.get()) {
            resetCounter();
        }
        synchronousTimer();

        if (counter.incrementAndGet() < 20) return;
        resetCounter();

        Bukkit.getServer().getOnlinePlayers().forEach(this::setKeepAliveTime);

        ServerUtil.sendConsoleMessage(Component.text("Reset player alive time!", NamedTextColor.GREEN));
    }

    private void resetCounter() {
        counter.set(0);
    }

    private void synchronousTimer() {
        timer.set(currentTimeMillis());
    }

    @SneakyThrows
    private void setKeepAliveTime(Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        long millis = net.minecraft.Util.getMillis();

        // mock ServerGamePacketListenerImpl update alive time
        try {
            KEEP_ALIVE_TIME.set(connection, millis + 100);
        } catch (IllegalAccessException e) {
            throw new BaioLibInternalException(e);
        }

        if (!isDebugging) {
            isDebugging = true;
        }

        connection.send(new ClientboundKeepAlivePacket(millis));

        System.out.printf("KEEP_ALIVE_PENDING: %s, KEEP_ALIVE_TIME: %s, KEEP_ALIVE_CHALLENGE: %s%n", KEEP_ALIVE_PENDING.get(connection), KEEP_ALIVE_TIME.get(connection), KEEP_ALIVE_CHALLENGE.get(connection));
    }

    private final static Field KEEP_ALIVE_TIME;
    private final static Field KEEP_ALIVE_PENDING;
    private final static Field KEEP_ALIVE_CHALLENGE;
    private final static Timer TASK_SCHEDULER;
    private final static KeepAliveUpdater instance;

    static {
        try {
            KEEP_ALIVE_TIME = ServerGamePacketListenerImpl.class.getDeclaredField("g");
            KEEP_ALIVE_PENDING = ServerGamePacketListenerImpl.class.getDeclaredField("h");
            KEEP_ALIVE_CHALLENGE = ServerGamePacketListenerImpl.class.getDeclaredField("i");
            KEEP_ALIVE_TIME.setAccessible(true);
            KEEP_ALIVE_PENDING.setAccessible(true);
            KEEP_ALIVE_CHALLENGE.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new BaioLibInternalException(e);
        }

        TASK_SCHEDULER = new Timer();

        instance = new KeepAliveUpdater();
    }

    /**
     * doStart auto update task
     */
    public static void doStart() {
        TASK_SCHEDULER.schedule(instance, 0L, 1000L);
    }

    /**
     * close auto update task
     */
    public static void doStop() {
        TASK_SCHEDULER.cancel();
    }
}
