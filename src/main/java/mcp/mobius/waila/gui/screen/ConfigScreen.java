package mcp.mobius.waila.gui.screen;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import mcp.mobius.waila.gui.widget.ConfigListWidget;
import mcp.mobius.waila.gui.widget.value.ConfigValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

public abstract class ConfigScreen extends Screen {

    private final Screen parent;
    private final Runnable saver;
    private final Runnable canceller;

    @SuppressWarnings("unchecked")
    private final List<GuiEventListener> children = (List<GuiEventListener>) children();
    private ConfigListWidget options;

    public ConfigScreen(Screen parent, Component title, Runnable saver, Runnable canceller) {
        super(title);

        this.parent = parent;
        this.saver = saver;
        this.canceller = canceller;
    }

    public ConfigScreen(Screen parent, Component title) {
        this(parent, title, null, null);
    }

    @Override
    public void init() {
        super.init();

        options = getOptions();
        children.add(options);
        setFocused(options);

        if (saver != null && canceller != null) {
            addRenderableWidget(new Button(width / 2 - 102, height - 25, 100, 20, new TranslatableComponent("gui.done"), w -> {
                options.save();
                saver.run();
                onClose();
            }));
            addRenderableWidget(new Button(width / 2 + 2, height - 25, 100, 20, new TranslatableComponent("gui.cancel"), w -> {
                canceller.run();
                onClose();
            }));
        } else {
            addRenderableWidget(new Button(width / 2 - 50, height - 25, 100, 20, new TranslatableComponent("gui.done"), w -> {
                options.save();
                onClose();
            }));
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        options.render(matrices, mouseX, mouseY, partialTicks);
        drawCenteredString(matrices, font, title.getString(), width / 2, 12, 16777215);
        super.render(matrices, mouseX, mouseY, partialTicks);

        if (mouseY < 32 || mouseY > height - 32)
            return;

        options.getChildAt(mouseX, mouseY).ifPresent(element -> {
            if (element instanceof ConfigValue<?> value) {

                if (I18n.exists(value.getDescription())) {
                    String title = value.getTitle().getString();
                    List<FormattedCharSequence> tooltip = Lists.newArrayList(new TextComponent(title).getVisualOrderText());
                    tooltip.addAll(font.split(new TranslatableComponent(value.getDescription()).withStyle(ChatFormatting.GRAY), 200));
                    renderTooltip(matrices, tooltip, mouseX, mouseY);
                }
            }
        });
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    public void addListener(GuiEventListener listener) {
        children.add(listener);
    }

    public abstract ConfigListWidget getOptions();

}
