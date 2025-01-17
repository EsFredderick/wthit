package mcp.mobius.waila.gui.widget.value;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import mcp.mobius.waila.gui.widget.ConfigListWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigValue<T> extends ConfigListWidget.Entry {


    protected final Consumer<T> save;
    protected final String translationKey;

    @Nullable
    protected final T defaultValue;

    private final Component title;
    private final String description;
    private final Button resetButton;

    private T value;
    private int x;

    public ConfigValue(String translationKey, T value, @Nullable T defaultValue, Consumer<T> save) {
        this.translationKey = translationKey;
        this.title = new TranslatableComponent(translationKey);
        this.description = translationKey + "_desc";
        this.value = value;
        this.save = save;
        this.defaultValue = defaultValue;
        this.resetButton = defaultValue == null ? null
            : new Button(0, 0, 40, 20, new TranslatableComponent("controls.reset"), button -> resetValue());
    }

    @Override
    public final void render(PoseStack matrices, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
        super.render(matrices, index, rowTop, rowLeft, width, height, mouseX, mouseY, hovered, deltaTime);

        client.font.drawShadow(matrices, title.getString(), rowLeft, rowTop + (height - client.font.lineHeight) / 2f, 16777215);

        int w = width;
        if (resetButton != null) {
            w -= resetButton.getWidth() + 2;
            resetButton.x = rowLeft + width - resetButton.getWidth();
            resetButton.y = rowTop + (height - resetButton.getHeight()) / 2;
            resetButton.active = !getValue().equals(defaultValue);
            resetButton.render(matrices, mouseX, mouseY, deltaTime);
        }

        drawValue(matrices, w, height, rowLeft, rowTop, mouseX, mouseY, hovered, deltaTime);
        this.x = rowLeft;
    }

    public void save() {
        save.accept(getValue());
    }

    public GuiEventListener getListener() {
        return null;
    }

    @Nullable
    public Button getResetButton() {
        return resetButton;
    }

    public Component getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getX() {
        return x;
    }

    public final T getValue() {
        return value;
    }

    public final void setValue(T value) {
        this.value = value;
    }

    protected void resetValue() {
        setValue(defaultValue);
    }

    protected abstract void drawValue(PoseStack matrices, int width, int height, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks);

}
