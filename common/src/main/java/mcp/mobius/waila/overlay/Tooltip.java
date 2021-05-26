package mcp.mobius.waila.overlay;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.config.PluginConfig;
import mcp.mobius.waila.config.WailaConfig;
import mcp.mobius.waila.config.WailaConfig.ConfigOverlay.ConfigOverlayColor;
import mcp.mobius.waila.config.WailaConfig.ConfigOverlay.Position.HorizontalAlignment;
import mcp.mobius.waila.config.WailaConfig.ConfigOverlay.Position.VerticalAlignment;
import mcp.mobius.waila.mixin.AccessorBossBarHud;
import mcp.mobius.waila.util.TaggedText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static mcp.mobius.waila.config.WailaConfig.ConfigOverlay.Position;
import static mcp.mobius.waila.overlay.DisplayUtil.drawGradientRect;
import static mcp.mobius.waila.overlay.DisplayUtil.enable2DRender;
import static mcp.mobius.waila.overlay.DisplayUtil.renderStack;

public class Tooltip {

    public static Consumer<List<Text>> onCreate;
    public static Function<Rectangle, Rectangle> onPreRender;
    public static Consumer<Rectangle> onPostRender;

    static boolean shouldRender = false;

    private static final List<Text> LINES = new ObjectArrayList<>();
    private static final Object2IntOpenHashMap<Text> LINE_HEIGHT = new Object2IntOpenHashMap<>();

    private static final Supplier<Rectangle> RENDER_RECT = Suppliers.memoize(Rectangle::new);
    private static final Supplier<Rectangle> RECT = Suppliers.memoize(Rectangle::new);

    private static boolean showItem;
    private static int topOffset = 0;

    private static boolean started = false;

    public static void start() {
        LINES.clear();
        LINE_HEIGHT.clear();
        started = true;
    }

    public static void addLines(List<Text> lines) {
        Preconditions.checkState(started);
        lines.forEach(c -> {
            Text text = c;
            if (text instanceof TaggedText) {
                text = ((ITaggableList<Identifier, Text>) lines).getTag(((TaggedText) text).getTag());
            }

            LINES.add(text);
        });
    }

    public static void addLine(Text line) {
        LINES.add(line);
    }

    public static void setShowItem(boolean showItem) {
        Preconditions.checkState(started);
        Tooltip.showItem = showItem;
    }

    public static void finish() {
        Preconditions.checkState(started);
        onCreate.accept(LINES);

        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();

        float scale = Waila.config.get().getOverlay().getScale();
        Position pos = Waila.config.get().getOverlay().getPosition();

        int w = 0;
        int h = 0;
        for (Text line : LINES) {
            int lineW;
            int lineH;

            if (line instanceof DrawableText) {
                Dimension size = ((DrawableText) line).getSize();
                lineW = size.width;
                lineH = size.height;
            } else {
                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                lineW = textRenderer.getWidth(line);
                lineH = textRenderer.fontHeight + 1;
            }

            w = Math.max(w, lineW);
            h += lineH;
            LINE_HEIGHT.put(line, lineH);
        }

        topOffset = 0;
        if (hasItem()) {
            if (h < 16) {
                topOffset = (16 - h) / 2;
            }

            w = Math.max(w, 16);
            h = Math.max(h, 16);
        }

        w += hasItem() ? 30 : 10;
        h += 8;

        int windowW = (int) (window.getScaledWidth() / scale);
        int windowH = (int) (window.getScaledHeight() / scale);

        HorizontalAlignment anchorX = pos.getAnchorX();
        VerticalAlignment anchorY = pos.getAnchorY();

        HorizontalAlignment alignX = pos.getAlignX();
        VerticalAlignment alignY = pos.getAlignY();

        double x = windowW * anchorX.multiplier - w * alignX.multiplier + pos.getX();
        double y = windowH * anchorY.multiplier - h * alignY.multiplier + pos.getY();

        if (anchorX == HorizontalAlignment.CENTER && anchorY == VerticalAlignment.TOP) {
            y += ((AccessorBossBarHud) client.inGameHud.getBossBarHud()).getBossBars().size() * 19;
        }

        RECT.get().setRect(x, y, w, h);

        started = false;
    }

    public static boolean hasItem() {
        return showItem && PluginConfig.INSTANCE.get(WailaConstants.CONFIG_SHOW_ITEM) && !Raycast.getDisplayItem().isEmpty();
    }

    public static void render(MatrixStack matrices, float delta) {
        if (!shouldRender) {
            return;
        }

        WailaConfig config = Waila.config.get();
        MinecraftClient.getInstance().getProfiler().push("Waila Overlay");
        RenderSystem.getModelViewStack().push();
        RenderSystem.applyModelViewMatrix();

        float scale = config.getOverlay().getScale();
        RenderSystem.getModelViewStack().scale(scale, scale, 1.0F);

        enable2DRender();

        Rectangle rect = RENDER_RECT.get();
        rect.setRect(RECT.get());

        rect = onPreRender.apply(rect);
        if (rect == null) {
            RenderSystem.enableDepthTest();
            RenderSystem.getModelViewStack().pop();
            MinecraftClient.getInstance().getProfiler().pop();
            return;
        }

        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;

        ConfigOverlayColor color = config.getOverlay().getColor();
        int bg = color.getBackgroundColor();
        int gradStart = color.getGradientStart();
        int gradEnd = color.getGradientEnd();

        matrices.push();
        matrices.scale(scale, scale, 1.0f);

        drawGradientRect(matrices, x + 1, y, w - 1, 1, bg, bg);
        drawGradientRect(matrices, x + 1, y + h, w - 1, 1, bg, bg);
        drawGradientRect(matrices, x + 1, y + 1, w - 1, h - 1, bg, bg);
        drawGradientRect(matrices, x, y + 1, 1, h - 1, bg, bg);
        drawGradientRect(matrices, x + w, y + 1, 1, h - 1, bg, bg);
        drawGradientRect(matrices, x + 1, y + 2, 1, h - 3, gradStart, gradEnd);
        drawGradientRect(matrices, x + w - 1, y + 2, 1, h - 3, gradStart, gradEnd);

        drawGradientRect(matrices, x + 1, y + 1, w - 1, 1, gradStart, gradStart);
        drawGradientRect(matrices, x + 1, y + h - 1, w - 1, 1, gradEnd, gradEnd);

        RenderSystem.enableBlend();

        int textX = x + (hasItem() ? 26 : 6);
        int textY = y + 6 + topOffset;

        for (Text line : LINES) {
            if (line instanceof DrawableText) {
                ((DrawableText) line).render(matrices, textX, textY, delta);
            } else {
                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                textRenderer.drawWithShadow(matrices, line, textX, textY, color.getFontColor());
            }

            textY += LINE_HEIGHT.getInt(line);
        }

        RenderSystem.disableBlend();
        matrices.pop();

        onPostRender.accept(rect);

        if (hasItem()) {
            renderStack(x + 5, y + h / 2 - 8, Raycast.getDisplayItem());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();
        MinecraftClient.getInstance().getProfiler().pop();
    }

}