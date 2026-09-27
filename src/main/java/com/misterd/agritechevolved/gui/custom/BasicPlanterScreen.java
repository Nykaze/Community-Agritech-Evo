package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.compat.jei.ATJeiBridge;
import com.misterd.agritechevolved.compat.jei.PlanterRecipeCategory;
import com.misterd.agritechevolved.trait.PlantTraits;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BasicPlanterScreen extends AbstractContainerScreen<BasicPlanterMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            new ResourceLocation("community_agritechevolved", "textures/gui/basic_planter_gui.png");
    private static final int GUI_HEIGHT = 172;

    public BasicPlanterScreen(BasicPlanterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = GUI_HEIGHT - 94;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI_TEXTURE,
                this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);

        float growthProgress = this.menu.getGrowthProgress() / 100.0F;
        if (growthProgress > 0.0F) {
            int barHeight = (int) (54.0F * growthProgress);
            int barY = this.topPos + 17 + 54 - barHeight;
            graphics.blit(GUI_TEXTURE,
                    this.leftPos + 40, barY,
                    176.0F, (float) (54 - barHeight),
                    6, barHeight, 256, 256);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (mouseX >= this.leftPos + 40 && mouseX <= this.leftPos + 46
                && mouseY >= this.topPos + 18 && mouseY <= this.topPos + 71) {
            float progress = this.menu.getGrowthProgress() / 100.0F;
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.growth_progress"),
                    Component.literal(String.format("%.1f%%", progress * 100.0F))
                            .withStyle(ChatFormatting.GREEN),
                    Component.translatable("tooltip.community_agritechevolved.view_recipes")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(8, 18, 16, 16, mouseX, mouseY) && menu.slots.get(36).getItem().isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.slot.plant")
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(8, 18, 16, 16, mouseX, mouseY)) {
            ItemStack plant = menu.slots.get(36).getItem();
            if (PlantTraits.hasTraits(plant)) {
                List<Component> lines = new java.util.ArrayList<>();
                lines.add(plant.getHoverName());
                lines.addAll(PlantTraits.of(plant).asTooltipLines());
                graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
                return;
            }
        }
        if (isHovering(8, 54, 16, 16, mouseX, mouseY) && menu.slots.get(37).getItem().isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.slot.soil")
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(152, 18, 16, 16, mouseX, mouseY) && menu.slots.get(38).getItem().isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.slot.fertilizer")
            ), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double mx = mouseX, my = mouseY;
            if (mx >= this.leftPos + 40 && mx <= this.leftPos + 46
                    && my >= this.topPos + 18 && my <= this.topPos + 71) {
                ATJeiBridge.showPlanterRecipes();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}