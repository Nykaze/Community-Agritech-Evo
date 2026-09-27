package com.misterd.agritechevolved.client.ber;

import com.misterd.agritechevolved.block.custom.AdvancedPlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AdvancedPlanterBlockEntityRenderer implements BlockEntityRenderer<AdvancedPlanterBlockEntity> {

    public AdvancedPlanterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AdvancedPlanterBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        boolean cloched = be.getBlockState().getValue(AdvancedPlanterBlock.CLOCHED);
        ItemStack soilStack = be.getStack(1);
        ItemStack plantStack = be.getStack(0);
        float growthProgress = be.getGrowthProgress();
        int growthStage = be.getGrowthStage();
        boolean isTree = PlanterBlockEntityRenderer.isTreePlant(plantStack);
        boolean soilIsWater = PlanterBlockEntityRenderer.isWaterBucket(soilStack);
        double distanceSq = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                .distanceToSqr(Vec3.atCenterOf(be.getBlockPos()));

        PlanterBlockEntityRenderer.renderShared(cloched, distanceSq, soilStack, soilIsWater, plantStack,
                isTree, growthStage, growthProgress, be.getLevel(),
                packedLight, packedOverlay, poseStack, bufferSource);
    }
}
