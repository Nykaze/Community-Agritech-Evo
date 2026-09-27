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

public class CapacitorScreen extends AbstractContainerScreen<CapacitorMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            new ResourceLocation("community_agritechevolved", "textures/gui/capacitor_gui.png");

    private static final int GUI_W = 176, GUI_H = 154;

    private static final int BAR_X = 8;
    private static final int BAR_Y = 19;
    private static final int BAR_W = 160;
    private static final int BAR_H = 34;

    public CapacitorScreen(CapacitorMenu menu, Inventory playerInventory, Component title) {
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

        int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
        if (maxEnergy > 0) {
            int fillWidth = (int) (BAR_W * (double) energy / maxEnergy);
            if (fillWidth > 0) {
                graphics.blit(GUI_TEXTURE,
                        this.leftPos + BAR_X, this.topPos + BAR_Y,
                        0.0F, 154.0F,
                        fillWidth, BAR_H, 256, 256);
            }
        }

        Component energyText = Component.literal(NumberFormat.getNumberInstance(Locale.US).format(energy) + " FE");
        int barCenterX = this.leftPos + BAR_X + BAR_W / 2;
        int barCenterY = this.topPos  + BAR_Y + BAR_H / 2 - this.font.lineHeight;
        graphics.drawString(this.font, energyText,
                barCenterX - this.font.width(energyText) / 2, barCenterY, 0xFFFFFFFF, true);

        if (maxEnergy > 0) {
            Component pctText = Component.literal(String.format("%.1f%%", (double) energy * 100.0 / maxEnergy));
            graphics.drawString(this.font, pctText,
                    barCenterX - this.font.width(pctText) / 2, barCenterY + 10, 0xFFCCCCCC, true);
        }

    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isOver(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
            int transferRate = menu.getTransferRate();
            String tierName = menu.getTierName();
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
            double pct = maxEnergy > 0 ? (double) energy * 100.0 / maxEnergy : 0.0;
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.capacitor.title", tierName).withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.energy_storage").withStyle(ChatFormatting.YELLOW),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.energy",
                            fmt.format(energy), fmt.format(maxEnergy)).withStyle(ChatFormatting.WHITE),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.energy_percentage",
                            String.format("%.1f", pct)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.transfer_rate",
                            fmt.format(transferRate)).withStyle(ChatFormatting.AQUA)
            ), mouseX, mouseY);
            return;
        }

        if (isOver(BAR_X, 6, BAR_W, 8, mouseX, mouseY)) {
            String tierName = menu.getTierName();
            int transferRate = menu.getTransferRate();
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.capacitor.title", tierName).withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.description").withStyle(ChatFormatting.YELLOW),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.max_transfer",
                            NumberFormat.getNumberInstance(Locale.US).format(transferRate)).withStyle(ChatFormatting.AQUA)
            ), mouseX, mouseY);
            return;
        }

        if (isOver(BAR_X, 50, BAR_W, 15, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("tooltip.community_agritechevolved.capacitor.information").withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.info_stores").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.info_balances").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.community_agritechevolved.capacitor.info_automatic").withStyle(ChatFormatting.GRAY)
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