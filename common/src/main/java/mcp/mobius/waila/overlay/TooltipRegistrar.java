package mcp.mobius.waila.overlay;

import java.util.EnumMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.config.ConfigEntry;
import mcp.mobius.waila.config.PluginConfig;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public enum TooltipRegistrar implements IRegistrar {

    INSTANCE;

    public final TooltipRegistry<IBlockComponentProvider> blockOverride = new TooltipRegistry<>();
    public final TooltipRegistry<IBlockComponentProvider> blockItem = new TooltipRegistry<>();
    public final TooltipRegistry<IServerDataProvider<BlockEntity>> blockData = new TooltipRegistry<>();
    public final Map<TooltipPosition, TooltipRegistry<IBlockComponentProvider>> blockComponent = Util.make(new EnumMap<>(TooltipPosition.class), map -> {
        for (TooltipPosition key : TooltipPosition.values()) {
            map.put(key, new TooltipRegistry<>());
        }
    });

    public final TooltipRegistry<IEntityComponentProvider> entityOverride = new TooltipRegistry<>();
    public final TooltipRegistry<IEntityComponentProvider> entityItem = new TooltipRegistry<>();
    public final TooltipRegistry<IServerDataProvider<Entity>> entityData = new TooltipRegistry<>();
    public final Map<TooltipPosition, TooltipRegistry<IEntityComponentProvider>> entityComponent = Util.make(new EnumMap<>(TooltipPosition.class), map -> {
        for (TooltipPosition key : TooltipPosition.values()) {
            map.put(key, new TooltipRegistry<>());
        }
    });

    public final Map<Identifier, ITooltipRenderer> renderer = new Object2ObjectOpenHashMap<>();

    private boolean locked = false;

    @Override
    public void addConfig(Identifier key, boolean defaultValue) {
        assertLock();
        PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, false));
    }

    @Override
    public void addSyncedConfig(Identifier key, boolean defaultValue) {
        assertLock();
        PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, true));
    }

    @Override
    public <T> void addOverride(IBlockComponentProvider provider, Class<T> clazz, int priority) {
        assertLock();
        assertPriority(priority);
        assertBlock(clazz);
        blockOverride.add(clazz, provider, priority);
    }

    @Override
    public <T> void addDisplayItem(IBlockComponentProvider provider, Class<T> clazz, int priority) {
        assertLock();
        assertPriority(priority);
        assertBlock(clazz);
        blockItem.add(clazz, provider, priority);
    }

    @Override
    public <T> void addComponent(IBlockComponentProvider provider, TooltipPosition position, Class<T> clazz, int priority) {
        assertLock();
        assertPriority(priority);
        assertBlock(clazz);
        blockComponent.get(position).add(clazz, provider, priority);
    }

    @Override
    public <T> void addBlockData(IServerDataProvider<BlockEntity> provider, Class<T> clazz) {
        assertLock();
        assertBlock(clazz);
        blockData.add(clazz, provider, 0);
    }

    @Override
    public <T> void addOverride(IEntityComponentProvider provider, Class<T> clazz, int priority) {
        assertLock();
        assertPriority(priority);
        assertEntity(clazz);
        entityOverride.add(clazz, provider, priority);
    }

    @Override
    public <T> void addDisplayItem(IEntityComponentProvider provider, Class<T> clazz, int priority) {
        assertLock();
        assertPriority(priority);
        assertEntity(clazz);
        entityItem.add(clazz, provider, priority);
    }

    @Override
    public <T> void addComponent(IEntityComponentProvider provider, TooltipPosition position, Class<T> clazz, int priority) {
        assertLock();
        assertPriority(priority);
        assertEntity(clazz);
        entityComponent.get(position).add(clazz, provider, priority);
    }

    @Override
    public <T> void addEntityData(IServerDataProvider<Entity> provider, Class<T> clazz) {
        assertLock();
        assertEntity(clazz);
        entityData.add(clazz, provider, 0);
    }

    @Override
    public void addRenderer(Identifier id, ITooltipRenderer renderer) {
        this.renderer.put(id, renderer);
    }

    public void lock() {
        locked = true;
    }

    private void assertLock() {
        Preconditions.checkState(!locked,
            "Tried to register new component after the registrar is locked");
    }

    private void assertPriority(int priority) {
        Preconditions.checkArgument(priority >= 0,
            "Priority must be equals or more than 0");
    }

    private <T> void assertBlock(Class<T> clazz) {
        Preconditions.checkArgument(Block.class.isAssignableFrom(clazz) || BlockEntity.class.isAssignableFrom(clazz),
            "Class must be a Block or BlockEntity");
    }

    private <T> void  assertEntity(Class<T> clazz) {
        Preconditions.checkArgument(Entity.class.isAssignableFrom(clazz),
            "Class must be an Entity");
    }

}
