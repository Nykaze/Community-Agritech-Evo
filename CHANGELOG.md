# Changelog

All notable changes to this project will be documented here.

Versions below `[3.1.0.5+neoforge-mc26.2]` are the history of the original
Agritech: Evolved by MisterD Modding, kept here for reference. This fork
restarts its own version line at `0.1b` because it is not a continuation of
those releases.

## [0.1b+forge-mc1.20.1] - 2026-09-27
First release of Community AgriTech Evo, the unofficial Forge 1.20.1 backport.

### Added
- Crop trait system: every planted seed rolls Growth, Yield, Resistance and
  Mutability traits. Traits are stored in the seed's NBT, shown in the planter
  GUI and in Jade, and can mutate upward on their own over time.
- Optional JEI and Jade integrations. Both are now guarded at runtime, so the
  mod loads and runs with neither installed.

### Fixed
- Harvest output no longer duplicates items when a planter's drop stack cannot
  fit the whole harvest.
- Duplicate recipe error: `DurabilityShapelessRecipeSerializer` hardcoded a
  single recipe id, so all five tilling recipes collided and all but one were
  dropped by the recipe manager.
- Tilling recipes required a `minecraft:barrier` instead of a hoe: the shared
  ingredient codec expanded tag ingredients through `getItems()`, and an
  unbound tag serialises to an empty item list during datagen.
- Generated resources used the pre-1.20.1 folder names (`recipe/`,
  `loot_table/`, `advancement/`, `assets/.../items/`), which 1.20.1 ignores.
  Migrated to `recipes/`, `loot_tables/`, `advancements/` and `models/`.

### Changed
- Mod id is now `community_agritechevolved`, so existing worlds, configs and
  datapacks from the original are not compatible.
- Base version reset to `0.1b` to start a separate version line from the
  original mod.

## [2.0.15.3+neoforge-mc26.1.2] - 2026-05-24
### Added
- Japanese translation (ja_jp) - thanks hamu6251ren0725-hue!

## [2.0.15.4+neoforge-mc26.1.2] - 2026-05-26
### Fixed
- Fallback recipe for planters

## [2.0.15.5+neoforge-mc26.1.2] - 2026-06-04
### Maint
- Version Bump

## [2.1.0+neoforge-mc26.1.2] - 2026-06-12
### Changed
- Compatibility layer is now fully data-driven. Seeds, saplings, soils, fertilizers and their values are defined via recipes and NeoForge datamaps — fully packdev-accessible via datapacks and KubeJS.
- Composter now uses vanilla compostable values. No more hardcoded entries. Anything that can be composted in a vanilla composter can also be used in the ATE composter
### Added
- Tooltip injection on all valid soil blocks and fertilizers showing their planter growth modifiers
### Bug Fixes
- Fixed fertilizer item duplicating when right-clicking the Advanced Planter
- Fixed a bug that allowed more than one upgrade module to be inserted into the Advanced Planter module slots

## [2.1.1+neoforge-mc26.1.2] - 2026-06-14
### Performance
- Planters no longer scan the full recipe list every tick. Recipe lookups are now cached per seed item and invalidated on datapack reload, reducing server tick time significantly at scale
- Valid soil items are now cached per datapack revision, eliminating repeated recipe scans on inventory interaction
- Block update packets now only fire on growth stage change instead of every second per planter
- Output item transfer now runs only on harvest instead of every tick

## [2.1.2+neoforge-mc26.1.2] - 2026-06-16
### Added
- New compat API for planters: `PlanterProcessingTimeEvent`, `PlanterPreHarvestEvent`, `PlanterPostHarvestEvent` (in `com.misterd.agritechevolved.integration`). Allows other mods to read/modify seed stats, override growth time, and adjust harvest output via NeoForge events. Fired by the basic and advanced planter
- `PlanterPreHarvestEvent` writes the returned seed back into the planter's seed slot, allowing persistent stat changes on the planted seed itself without needing to cycle harvested seeds back through the output

### Removed
- Removed the `/agritechevolved reload` command as it no longer makes sense nor works with the new data-driven approach

## [2.1.2.1+neoforge-mc26.1.2] - 2026-06-16
### Fixed
- Fixed data components (NBT) being stripped from items when extracted through menus or scaled through yield modifiers

## [3.0.0+neoforge-mc26.2] - 2026-06-19
- Port to 26.2

## [3.0.0.1+neoforge-mc26.2] - 2026-06-20
### Updated
- Updated JEI and Jade

## [3.0.0.2+neoforge-mc26.2] - 2026-06-24
### Fixed
- Made it so MA farmlands can't be downgraded in the planter

## [3.1.0+neoforge-mc26.2] - 2026-07-05
### Added
- Added Silo and Fertilizer Spreader blocks. Silo pulls drops from all planters in range, Fertilizer Spreader pushes fertilizer to all planters in range
- Added range upgrades for the Silo and Fertilizer Spreader blocks
- Added config options for both blocks

## [3.1.0.1+neoforge-mc26.2] - 2026-07-05
### Fixed
- Better performance for the silo and spreader

## [3.1.0.2+neoforge-mc26.2] - 2026-07-07
### Added
- Added Biomes O' Plenty, Oh The Biomes We've Gone, Regions Unexplored, and Silent's Gems compatibility

## [3.1.0.3+neoforge-mc26.2] - 2026-08-29
### Added
-  New terracotta planters (all 17 variants)
- New vanilla crops compatability: all coral, short grass, and ferns

### Changed

- Updated basic planters' recipes to be less annoying 

## [3.1.0.4+neoforge-mc26.2] - 2026-09-10
### Fixed
- Issue #7: fixed the range upgrades recipes to use the correct item

## [3.1.0.5+neoforge-mc26.2] - 2026-09-22
### Fixed
- Fresh textures
