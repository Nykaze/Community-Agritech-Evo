package com.misterd.agritechevolved.client.ber;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritechevolved.util.RegistryHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class PlanterBlockEntityRenderer implements BlockEntityRenderer<PlanterBlockEntity> {

    private static final ResourceLocation WATER_STILL = new ResourceLocation("minecraft:block/water_still");
    private static final ModelResourceLocation CLOCHE_DOME_MODEL =
            new ModelResourceLocation(new ResourceLocation(AgritechEvolved.MODID, "block/cloche_dome"), "normal");

    public PlanterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PlanterBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        boolean cloched = be.getBlockState().getValue(PlanterBlock.CLOCHED);
        ItemStack soilStack = be.getStack(1);
        ItemStack plantStack = be.getStack(0);
        float growthProgress = be.getGrowthProgress();
        int growthStage = be.getGrowthStage();
        boolean isTree = isTreePlant(plantStack);
        boolean soilIsWater = isWaterBucket(soilStack);
        double distanceSq = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                .distanceToSqr(Vec3.atCenterOf(be.getBlockPos()));

        renderShared(cloched, distanceSq, soilStack, soilIsWater, plantStack,
                isTree, growthStage, growthProgress, be.getLevel(),
                packedLight, packedOverlay, poseStack, bufferSource);
    }

    public static void renderShared(
            boolean cloched, double distanceSq,
            ItemStack soilStack, boolean soilIsWater, ItemStack plantStack,
            boolean isTree, int growthStage, float growthProgress, @Nullable Level level,
            int packedLight, int packedOverlay, PoseStack poseStack, MultiBufferSource bufferSource) {

        if (cloched && distanceSq <= 256.0) {
            renderClocheDome(packedLight, poseStack, bufferSource);
        }

        if (!soilStack.isEmpty()) {
            if (soilIsWater) {
                renderWater(packedLight, poseStack, bufferSource);
            } else {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.4, 0.5);
                poseStack.scale(1.3f, 0.65f, 1.3f);
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        soilStack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
                poseStack.popPose();
            }
        }

        if (!plantStack.isEmpty() && !soilStack.isEmpty() && plantStack.getItem() instanceof BlockItem) {
            BlockState plantState = isTree
                    ? ((BlockItem) plantStack.getItem()).getBlock().defaultBlockState()
                    : getCropBlockState(plantStack, growthStage);

            if (plantState != null) {
                poseStack.pushPose();
                if (isTree) {
                    float scale = 0.3f + growthProgress * 0.4f;
                    poseStack.translate(0.5, 0.58, 0.5);
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(-0.5, 0.0, -0.5);
                } else {
                    poseStack.translate(0.1725, 0.58, 0.1725);
                    poseStack.scale(0.65f, 0.65f, 0.65f);
                }
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(plantState, poseStack, bufferSource, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
    }

    private static void renderClocheDome(int packedLight, PoseStack poseStack, MultiBufferSource bufferSource) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(CLOCHE_DOME_MODEL);
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) return;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad quad : model.getQuads(null, Direction.UP, RandomSource.create(0))) {
            consumer.putBulkData(pose, quad, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    private static void renderWater(int packedLight, PoseStack poseStack, MultiBufferSource bufferSource) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(WATER_STILL);
        if (sprite == null) return;

        float y = 0.57f;
        float xMin = 0.175f, xMax = 0.825f;
        float zMin = 0.175f, zMax = 0.825f;
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        addWaterVertex(consumer, pose, xMin, y, zMin, u0, v0, packedLight);
        addWaterVertex(consumer, pose, xMin, y, zMax, u0, v1, packedLight);
        addWaterVertex(consumer, pose, xMax, y, zMax, u1, v1, packedLight);
        addWaterVertex(consumer, pose, xMax, y, zMin, u1, v0, packedLight);
    }

    private static void addWaterVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(0x3F, 0x76, 0xE4, 0xA0)
                .uv(u, v)
                .uv2(light, OverlayTexture.NO_OVERLAY)
                .normal(pose.normal(), 0f, 1f, 0f)
                .endVertex();
    }

    static boolean isWaterBucket(ItemStack stack) {
        return !stack.isEmpty() && RegistryHelper.getItemId(stack).equals("minecraft:water_bucket");
    }

    public static boolean isTreePlant(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) return false;
        BlockState def = bi.getBlock().defaultBlockState();
        return def.getProperties().stream().noneMatch(p -> p.getName().equals("age"));
    }

    @Nullable
    public static BlockState getCropBlockState(ItemStack stack, int age) {
        if (!(stack.getItem() instanceof BlockItem bi)) return null;
        BlockState def = bi.getBlock().defaultBlockState();
        for (Property<?> prop : def.getProperties()) {
            if (prop instanceof IntegerProperty ip && prop.getName().equals("age")) {
                int max = ip.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(7);
                return def.setValue(ip, Math.min(age, max));
            }
        }
        if (def.hasProperty(BlockStateProperties.AGE_7)) return def.setValue(BlockStateProperties.AGE_7, Math.min(age, 7));
        if (def.hasProperty(BlockStateProperties.AGE_3)) return def.setValue(BlockStateProperties.AGE_3, Math.min(age, 3));
        if (def.hasProperty(BlockStateProperties.AGE_5)) return def.setValue(BlockStateProperties.AGE_5, Math.min(age, 5));
        if (def.hasProperty(BlockStateProperties.AGE_15)) return def.setValue(BlockStateProperties.AGE_15, Math.min(age, 15));
        if (def.hasProperty(BlockStateProperties.AGE_25)) return def.setValue(BlockStateProperties.AGE_25, Math.min(age, 25));
        return def;
    }
}
