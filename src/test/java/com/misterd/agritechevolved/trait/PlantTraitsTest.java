package com.misterd.agritechevolved.trait;

import org.junit.jupiter.api.Test;

import net.minecraft.util.RandomSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantTraitsTest {

    private static PlantTraits all(TraitLevel level) {
        return new PlantTraits(level, level, level, level);
    }

    @Test
    void levelsAreOrderedAndBounded() {
        assertEquals(5, TraitLevel.count());
        assertEquals(TraitLevel.NONE, TraitLevel.byId(0));
        assertEquals(TraitLevel.VERY_HIGH, TraitLevel.byId(4));
        assertTrue(TraitLevel.VERY_HIGH.isMax());
        assertFalse(TraitLevel.HIGH.isMax());
    }

    @Test
    void byIdClampsOutOfRangeValues() {
        assertEquals(TraitLevel.NONE, TraitLevel.byId(-7));
        assertEquals(TraitLevel.VERY_HIGH, TraitLevel.byId(99));
    }

    @Test
    void nextSaturatesAtMax() {
        assertEquals(TraitLevel.LOW, TraitLevel.NONE.next());
        assertEquals(TraitLevel.VERY_HIGH, TraitLevel.VERY_HIGH.next());
    }

    @Test
    void byNameRoundTrips() {
        for (TraitLevel level : TraitLevel.values()) {
            assertSame(level, TraitLevel.byName(level.getSerializedName()));
        }
        assertEquals(TraitLevel.NONE, TraitLevel.byName("not_a_level"));
    }

    @Test
    void noneLevelIsMultiplicativelyNeutral() {
        assertEquals(1.0F, TraitLevel.NONE.getGrowthMultiplier());
        assertEquals(1.0F, TraitLevel.NONE.getYieldMultiplier());
        assertEquals(0.0F, TraitLevel.NONE.getProgressRetainedOnReset());
        assertEquals(0.0F, TraitLevel.NONE.getMutationChancePerTick());
    }

    @Test
    void everyNonNeutralLevelStrictlyImprovesGrowthAndYield() {
        float previousGrowth = 0.0F;
        float previousYield = 0.0F;
        for (TraitLevel level : TraitLevel.values()) {
            assertTrue(level.getGrowthMultiplier() > previousGrowth,
                    "growth must increase at " + level);
            assertTrue(level.getYieldMultiplier() > previousYield,
                    "yield must increase at " + level);
            previousGrowth = level.getGrowthMultiplier();
            previousYield = level.getYieldMultiplier();
        }
    }

    @Test
    void resistanceRetentionIsMonotonicAndBounded() {
        float previous = -1.0F;
        for (TraitLevel level : TraitLevel.values()) {
            float retained = level.getProgressRetainedOnReset();
            assertTrue(retained >= 0.0F && retained <= 1.0F, "retention out of range at " + level);
            assertTrue(retained >= previous, "retention must not decrease at " + level);
            previous = retained;
        }
        assertEquals(1.0F, TraitLevel.VERY_HIGH.getProgressRetainedOnReset());
    }

    @Test
    void mutationChanceIsPositiveAboveNone() {
        for (PlantTraits.TraitKind kind : PlantTraits.TraitKind.values()) {
            assertEquals(0.0F, all(TraitLevel.NONE).get(kind).getMutationChancePerTick());
        }
        for (TraitLevel level : TraitLevel.values()) {
            if (level == TraitLevel.NONE) continue;
            assertTrue(level.getMutationChancePerTick() > 0.0F, "no chance at " + level);
            assertTrue(level.getMutationChancePerTick() < 1.0F, "chance must be rare at " + level);
        }
    }

    @Test
    void rollAlwaysProducesReadableTraitsInRange() {
        RandomSource random = RandomSource.create(1234L);
        for (int i = 0; i < 2000; i++) {
            PlantTraits traits = PlantTraits.roll(RandomSource.create(random.nextLong()));
            assertNotNull(traits);
            for (PlantTraits.TraitKind kind : PlantTraits.TraitKind.values()) {
                TraitLevel level = traits.get(kind);
                assertTrue(level.getId() >= 0 && level.getId() < TraitLevel.count(),
                        "level out of range: " + level);
            }
        }
    }

    @Test
    void improveRaisesExactlyOneTraitByOneLevel() {
        RandomSource random = RandomSource.create(99L);
        for (int i = 0; i < 500; i++) {
            PlantTraits before = new PlantTraits(
                    TraitLevel.LOW, TraitLevel.LOW, TraitLevel.LOW, TraitLevel.LOW);
            PlantTraits after = before.improve(RandomSource.create(random.nextLong()));

            assertNotNull(after);
            int raised = 0;
            for (PlantTraits.TraitKind kind : PlantTraits.TraitKind.values()) {
                int delta = after.get(kind).getId() - before.get(kind).getId();
                assertTrue(delta == 0 || delta == 1, "trait jumped by " + delta);
                if (delta == 1) raised++;
            }
            assertEquals(1, raised, "exactly one trait should improve");
        }
    }

    @Test
    void improveNeverTouchesAMaxedTrait() {
        PlantTraits before = new PlantTraits(
                TraitLevel.VERY_HIGH, TraitLevel.HIGH, TraitLevel.VERY_HIGH, TraitLevel.VERY_HIGH);
        RandomSource random = RandomSource.create(7L);
        for (int i = 0; i < 200; i++) {
            PlantTraits after = before.improve(RandomSource.create(random.nextLong()));
            assertNotNull(after);
            assertEquals(TraitLevel.VERY_HIGH, after.growth());
            assertEquals(TraitLevel.VERY_HIGH, after.resistance());
            assertEquals(TraitLevel.VERY_HIGH, after.mutability());
            assertTrue(before.yield() != TraitLevel.VERY_HIGH,
                    "the only non-maxed trait should be the one that moves");
            assertTrue(after.yield() == TraitLevel.VERY_HIGH || after.yield().getId() > before.yield().getId(),
                    "yield must be the trait that improves");
        }
    }

    @Test
    void improveReturnsNullWhenFullyMaxed() {
        RandomSource random = RandomSource.create(3L);
        for (int i = 0; i < 50; i++) {
            assertNull(all(TraitLevel.VERY_HIGH).improve(RandomSource.create(random.nextLong())));
        }
    }

    @Test
    void improveWalksAFullTraitFromLowToVeryHighAndStops() {
        PlantTraits traits = new PlantTraits(
                TraitLevel.LOW, TraitLevel.LOW, TraitLevel.LOW, TraitLevel.LOW);
        RandomSource random = RandomSource.create(11L);

        for (int step = 0; step < 200; step++) {
            if (traits.isFullyMaxed()) break;
            PlantTraits next = traits.improve(RandomSource.create(random.nextLong()));
            assertNotNull(next, "improve returned null before reaching max");
            traits = next;
        }

        assertTrue(traits.isFullyMaxed(), "should reach max within 200 improvements");
        assertNull(traits.improve(RandomSource.create(1L)));
    }

    @Test
    void withReplacesOnlyTheRequestedTrait() {
        PlantTraits before = new PlantTraits(
                TraitLevel.LOW, TraitLevel.MEDIUM, TraitLevel.HIGH, TraitLevel.VERY_HIGH);
        PlantTraits after = before.with(PlantTraits.TraitKind.YIELD, TraitLevel.NONE);

        assertEquals(TraitLevel.LOW, after.growth());
        assertEquals(TraitLevel.NONE, after.yield());
        assertEquals(TraitLevel.HIGH, after.resistance());
        assertEquals(TraitLevel.VERY_HIGH, after.mutability());
    }

    @Test
    void nbtRoundTripPreservesEveryLevel() {
        for (TraitLevel growth : TraitLevel.values()) {
            for (TraitLevel yield : TraitLevel.values()) {
                PlantTraits original = new PlantTraits(
                        growth, yield, TraitLevel.MEDIUM, TraitLevel.HIGH);
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                original.writeToNBT(tag);

                PlantTraits restored = PlantTraits.readFromNBT(tag);
                assertNotNull(restored);
                assertEquals(original, restored, "PlantTraits needs equals() for this to hold");
            }
        }
    }

    @Test
    void readingUntaggedNbtYieldsNullSoRollsCanBeDetected() {
        net.minecraft.nbt.CompoundTag empty = new net.minecraft.nbt.CompoundTag();
        assertNull(PlantTraits.readFromNBT(empty));
        assertNull(PlantTraits.readFromNBT(null));
    }

    @Test
    void neutralTraitsAreDetectedAsNeutral() {
        assertTrue(PlantTraits.neutral().isNeutral());
        assertFalse(new PlantTraits(TraitLevel.NONE, TraitLevel.LOW,
                TraitLevel.NONE, TraitLevel.NONE).isNeutral());
    }

    @Test
    void tooltipLinesCoverAllFourTraits() {
        var lines = new PlantTraits(TraitLevel.LOW, TraitLevel.MEDIUM,
                TraitLevel.HIGH, TraitLevel.VERY_HIGH).asTooltipLines();
        assertEquals(4, lines.size());
    }
}
