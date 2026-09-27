package com.misterd.agritechevolved.block.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;

public class InfusedFarmlandBlock extends Block {

    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;

    public InfusedFarmlandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return !defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? Blocks.DIRT.defaultBlockState()
                : defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.is(Blocks.BAMBOO) || above.is(Blocks.CACTUS) || above.is(Blocks.BAMBOO_SAPLING)) return true;
        return !above.isSolid()
                || above.getBlock() instanceof FenceGateBlock
                || above.getBlock() instanceof MovingPistonBlock;
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, IPlantable plant) {
        if (facing != Direction.UP) return false;
        Block b = plant.getPlant(level, soilPosition).getBlock();
        return b instanceof BushBlock || b instanceof CropBlock || b instanceof StemBlock;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);

        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToMulch(null, state, level, pos);
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }

        if (moisture == 7 && random.nextFloat() < 0.45F) {
            bonemealCrop(level, pos, random);
        }
    }

    private void bonemealCrop(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos cropPos = pos.above();
        BlockState crop = level.getBlockState(cropPos);
        BlockState next = null;
        Block b = crop.getBlock();

        if (b instanceof CropBlock cropBlock) {
            if (!cropBlock.isMaxAge(crop)) {
                next = cropBlock.getStateForAge(cropBlock.getAge(crop) + 1);
            }
        } else if (b instanceof StemBlock) {
            int age = crop.getValue(StemBlock.AGE);
            if (age < 7) next = crop.setValue(StemBlock.AGE, age + 1);
        } else if (crop.is(Blocks.SWEET_BERRY_BUSH)) {
            int age = crop.getValue(SweetBerryBushBlock.AGE);
            if (age < 3) next = crop.setValue(SweetBerryBushBlock.AGE, age + 1);
        } else if (b instanceof BonemealableBlock bonemealable) {
            if (bonemealable.isValidBonemealTarget((LevelReader) level, cropPos, crop, true)) {
                bonemealable.performBonemeal(level, random, cropPos, crop);
            }
        }

        if (next != null) level.setBlockAndUpdate(cropPos, next);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (fallDistance > 0.0F) {
            entity.hurt(level.damageSources().fall(), fallDistance);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean movedByPiston) {
        if (!state.canSurvive(level, pos)) turnToMulch(null, state, level, pos);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) { return true; }

    public static void turnToMulch(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState mulch = pushEntitiesUp(state, ATEBlocks.MULCH.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, mulch);
    }

    private static BlockState pushEntitiesUp(BlockState oldState, BlockState newState, Level level, BlockPos pos) {
        VoxelShape oldShape = oldState.getCollisionShape(level, pos);
        VoxelShape newShape = newState.getCollisionShape(level, pos);
        if (!oldShape.isEmpty() && newShape.isEmpty()) return newState;

        double yOffset = newShape.max(Direction.Axis.Y) - oldShape.max(Direction.Axis.Y);
        if (yOffset > 0.0) {
            level.getEntities(null, oldShape.bounds().move(pos))
                    .forEach(e -> e.teleportTo(e.getX(), e.getY() + yOffset, e.getZ()));
        }
        return newState;
    }

    private static boolean shouldMaintainFarmland(BlockGetter level, BlockPos pos) {
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-6, 0, -6), pos.offset(6, 1, 6))) {
            if (level.getBlockState(nearby).is(BlockTags.CROPS)) return true;
        }
        return false;
    }

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-6, 0, -6), pos.offset(6, 1, 6))) {
            if (level.getFluidState(nearby).is(FluidTags.WATER)) return true;
        }
        return false;
    }
}