package com.misterd.agritechevolved.gui.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BiomassBurnerScreen extends AbstractContainerScreen<BiomassBurnerMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            new ResourceLocation("community_agritechevolved", "textures/gui/burner_gui.png");

    private static final int GUI_W = 176, GUI_H = 165;

    private static final int PROGRESS_BAR_X = 103;
    private static final int PROGRESS_BAR_Y = 28;
    private static final int PROGRESS_BAR_W = 6;
    private static final int PROGRESS_BAR_H = 24;
    private static final int PROGRESS_BAR_TEX_X = 176;
    private static final int PROGRESS_BAR_TEX_Y = 0;

    private static final int ENERGY_BAR_X = 163;
    private static final int ENERGY_BAR_Y = 14;
    private static final int ENERGY_BAR_W = 6;
    private static final int ENERGY_BAR_H = 54;
    private static final int ENERGY_BAR_TEX_X = 182;

    private static final int FUEL_SLOT_X = 79;
    private static final int FUEL_SLOT_Y = 31;
    private static final int FUEL_SLOT_W = 18;
    private static final int FUEL_SLOT_H = 18;

    public BiomassBurnerScreen(BiomassBurnerMenu menu, Inventory playerInventory, Component title) {
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
            int filled = (int) (PROGRESS_BAR_H * (double) progress / maxProgress);
            if (filled > 0) {
                graphics.blit(GUI_TEXTURE,
                        this.leftPos + PROGRESS_BAR_X, this.topPos + PROGRESS_BAR_Y + PROGRESS_BAR_H - filled,
                        (float) PROGRESS_BAR_TEX_X, (float) (PROGRESS_BAR_TEX_Y + PROGRESS_BAR_H - filled),
                        PROGRESS_BAR_W, filled, 256, 256);
            }
        }

        int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
        if (maxEnergy > 0) {
            int filled = (int) (ENERGY_BAR_H * (double) energy / maxEnergy);
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
        if (isOver(ENERGY_BAR_X, ENERGY_BAR_Y, ENERGY_BAR_W, ENERGY_BAR_H, mouseX, mouseY)) {
            int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
            double pct = maxEnergy > 0 ? (double) energy * 100.0 / maxEnergy : 0.0;
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.burner.energy",
                            fmt.format(energy), fmt.format(maxEnergy)).withStyle(ChatFormatting.YELLOW),
                    Component.translatable("tooltip.community_agritechevolved.burner.energy_percentage",
                            String.format("%.1f", pct)).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
            return;
        }

        if (isOver(PROGRESS_BAR_X, PROGRESS_BAR_Y - 1, PROGRESS_BAR_W, PROGRESS_BAR_H + 1, mouseX, mouseY)) {
            int progress = menu.getProgress(), maxProgress = menu.getMaxProgress();
            List<Component> tooltip;
            if (maxProgress > 0) {
                double pct = (double) progress * 100.0 / maxProgress;
                double remainingSecs = (double) (maxProgress - progress) / 20.0;
                tooltip = List.of(
                        Component.translatable("tooltip.community_agritechevolved.burner.burning_progress")
                                .withStyle(ChatFormatting.GOLD),
                        Component.translatable("tooltip.community_agritechevolved.burner.progress_percentage",
                                String.format("%.1f", pct)).withStyle(ChatFormatting.YELLOW),
                        Component.translatable("tooltip.community_agritechevolved.burner.time_remaining",
                                String.format("%.1f", remainingSecs)).withStyle(ChatFormatting.GRAY)
                );
            } else {
                tooltip = List.of(
                        Component.translatable("tooltip.community_agritechevolved.burner.no_fuel").withStyle(ChatFormatting.RED),
                        Component.translatable("tooltip.community_agritechevolved.burner.insert_fuel").withStyle(ChatFormatting.GRAY)
                );
            }
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            return;
        }

        if (isOver(FUEL_SLOT_X, FUEL_SLOT_Y, FUEL_SLOT_W, FUEL_SLOT_H, mouseX, mouseY)
                && menu.blockEntity.getStack(0).isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.burner.fuel_slot").withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.community_agritechevolved.burner.accepts").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.community_agritechevolved.burner.accepts_crude_biomass").withStyle(ChatFormatting.GREEN),
                    Component.translatable("tooltip.community_agritechevolved.burner.accepts_biomass").withStyle(ChatFormatting.GREEN),
                    Component.translatable("tooltip.community_agritechevolved.burner.accepts_compacted_biomass").withStyle(ChatFormatting.GREEN),
                    Component.translatable("tooltip.community_agritechevolved.burner.accepts_compacted_biomass_block").withStyle(ChatFormatting.GREEN)
            ), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }


    private boolean isOver(int wx, int wy, int ww, int wh, int mx, int my) {
        return mx >= this.leftPos + wx && mx <= this.leftPos + wx + ww
                && my >= this.topPos + wy && my <= this.topPos + wy + wh;
    }
}