package com.misterd.agritechevolved.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import java.util.List;

public class ClocheItem extends Item {
    public ClocheItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.community_agritechevolved.cloche.tooltip.line1"));
        tooltip.add(Component.translatable("item.community_agritechevolved.cloche.tooltip.line2"));
        tooltip.add(Component.translatable("item.community_agritechevolved.cloche.tooltip.line3"));
    }
}
