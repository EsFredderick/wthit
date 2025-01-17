package mcp.mobius.waila.integration.jei;

import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.hud.TooltipHandler;
import mcp.mobius.waila.util.CommonUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JeiRecipeLookup implements IModPlugin {

    private static final ResourceLocation ID = CommonUtil.id("jei");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jei) {
        WailaClient.onShowRecipeInput = () -> showRecipesGui(jei, IFocus.Mode.INPUT);
        WailaClient.onShowRecipeOutput = () -> showRecipesGui(jei, IFocus.Mode.OUTPUT);
    }

    private void showRecipesGui(IJeiRuntime jei, IFocus.Mode mode) {
        ItemStack stack = TooltipHandler.getStack();
        if (!stack.isEmpty()) {
            jei.getRecipesGui().show(jei.getRecipeManager().createFocus(mode, stack));
        }
    }

}
