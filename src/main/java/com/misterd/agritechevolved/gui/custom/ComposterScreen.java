package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.compat.jei.ATJeiBridge;
import com.misterd.agritechevolved.compat.jei.CompostRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ComposterScreen extends AbstractContainerScreen<ComposterMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            new ResourceLocation("community_agritechevolved", "textures/gui/composter_gui.png");

    private static final int GUI_W = 176, GUI_H = 172;

    private static final int PROGRESS_BAR_X = 85;
    private static final int PROGRESS_BAR_Y = 18;
    private static final int PROGRESS_BAR_W = 6;
    private static final int PROGRESS_BAR_H = 54;
    private static final int PROGRESS_BAR_TEX_X = 176;

    private static final int ENERGY_BAR_X = 163;
    private static final int ENERGY_BAR_Y = 18;
    private static final int ENERGY_BAR_W = 6;
    private static final int ENERGY_BAR_H = 54;
    private static final int ENERGY_BAR_TEX_X = 182;

    public ComposterScreen(ComposterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
        this.inventoryLabelY = GUI_H - 94;
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

        int progress = menu.getProgress(), maxProgress = menu.getMaxProgress();
        if (maxProgress > 0) {
            int filled = (int) (PROGRESS_BAR_H * (float) progress / maxProgress);
            if (filled > 0) {
                graphics.blit(GUI_TEXTURE,
                        this.leftPos + PROGRESS_BAR_X, this.topPos + PROGRESS_BAR_Y + PROGRESS_BAR_H - filled,
                        (float) PROGRESS_BAR_TEX_X, (float) (PROGRESS_BAR_H - filled),
                        PROGRESS_BAR_W, filled, 256, 256);
            }
        }

        int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
        if (maxEnergy > 0) {
            int filled = (int) (ENERGY_BAR_H * (float) energy / maxEnergy);
            if (filled > 0) {
                graphics.blit(GUI_TEXTURE,
                        this.leftPos + ENERGY_BAR_X, this.topPos + ENERGY_BAR_Y + ENERGY_BAR_H - filled,
                        (float) ENERGY_BAR_TEX_X, (float) (ENERGY_BAR_H - filled),
                        ENERGY_BAR_W, filled, 256, 256);
            }
        }

    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isOver(PROGRESS_BAR_X, PROGRESS_BAR_Y, PROGRESS_BAR_W, PROGRESS_BAR_H, mouseX, mouseY)) {
            int progress = menu.getProgress(), maxProgress = menu.getMaxProgress();
            int organicItems = menu.getOrganicItemsCollected(), requiredItems = menu.getRequiredOrganicItems();
            float pct = maxProgress > 0 ? (float) progress / maxProgress * 100.0F : 0.0F;
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.composting_info").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC),
                    Component.translatable("tooltip.community_agritechevolved.composting_progress").withStyle(ChatFormatting.GREEN),
                    Component.literal(String.format("%.1f%%", pct)).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.format("Organic Items: %d/%d", organicItems, requiredItems)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.community_agritechevolved.view_recipes")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            ), mouseX, mouseY);
            return;
        }

        if (isOver(ENERGY_BAR_X, ENERGY_BAR_Y, ENERGY_BAR_W, ENERGY_BAR_H, mouseX, mouseY)) {
            int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
            float pct = maxEnergy > 0 ? (float) energy / maxEnergy * 100.0F : 0.0F;
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.stored_energy").withStyle(ChatFormatting.YELLOW),
                    Component.literal(fmt.format(energy) + " / " + fmt.format(maxEnergy) + " FE").withStyle(ChatFormatting.GREEN),
                    Component.literal(String.format("%.1f%%", pct)).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
            return;
        }

        if (isHovering(134, 19, 16, 16, mouseX, mouseY) && menu.slots.get(51).getItem().isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.slot.upgrade")
            ), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0
                && isOver(PROGRESS_BAR_X, PROGRESS_BAR_Y, PROGRESS_BAR_W, PROGRESS_BAR_H,
                (int) mouseX, (int) mouseY)) {
            ATJeiBridge.showCompostRecipes();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOver(int wx, int wy, int ww, int wh, int mx, int my) {
        return mx >= this.leftPos + wx && mx <= this.leftPos + wx + ww
                && my >= this.topPos + wy && my <= this.topPos + wy + wh;
    }
}