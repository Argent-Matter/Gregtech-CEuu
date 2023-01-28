package net.nemezanevem.gregtech.api.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.util.function.Task;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = GregTech.MODID)
public class TaskScheduler {

    @Nullable
    public static TaskScheduler get(Level world) {
        return tasksPerWorld.get(world);
    }

    private static final Map<LevelAccessor, TaskScheduler> tasksPerWorld = new HashMap<>();

    private final List<Task> tasks = new ArrayList<>();
    private final List<Task> scheduledTasks = new ArrayList<>();
    private boolean running = false;

    public static void scheduleTask(Level world, Task task) {
        if (world.isClientSide) {
            throw new IllegalArgumentException("Attempt to schedule task on client world!");
        }
        tasksPerWorld.computeIfAbsent(world, k -> new TaskScheduler()).scheduleTask(task);
    }

    public void scheduleTask(Task task) {
        if (running) {
            scheduledTasks.add(task);
        } else {
            tasks.add(task);
        }
    }

    public void unload() {
        tasks.clear();
        scheduledTasks.clear();
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            TaskScheduler scheduler = tasksPerWorld.get(event.getLevel());
            if (scheduler != null) {
                scheduler.unload();
                tasksPerWorld.remove(event.getLevel());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (!event.level.isClientSide) {
            TaskScheduler scheduler = get(event.level);
            if (scheduler != null) {
                if (!scheduler.scheduledTasks.isEmpty()) {
                    scheduler.tasks.addAll(scheduler.scheduledTasks);
                    scheduler.scheduledTasks.clear();
                }
                scheduler.running = true;
                scheduler.tasks.removeIf(task -> !task.run());
                scheduler.running = false;
            }
        }
    }
}
