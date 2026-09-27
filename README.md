![ATE](https://raw.githubusercontent.com/deonjonker123/AgritechEvolved/refs/heads/26.1.2/ate_ban.png)

# Community AgriTech Evo

An unofficial Forge 1.20.1 backport of [Agritech: Evolved](https://github.com/deonjonker123/AgritechEvolved) by MisterD Modding.

Farming automation mod with planters, machines, modules, and power gen.

## Credits

- **Original mod** — Agritech: Evolved, created by **MisterD Modding**. All original design, code, assets and data are theirs, reused here under the MIT License.
- **1.20.1 backport** — done by **Nykaze**.

This is not an official release of Agritech: Evolved. See `CREDITS.txt` for the full breakdown and `LICENSE.txt` for licensing.

## Planters

**Basic Planter** — plant a seed or sapling, it grows by itself, drops go into whatever's underneath it. All 11 vanilla wood types.

**Advanced Planter** — Planter that runs on FE, and has speed and yield upgrade slots.

## Cloche

Glass dome that attaches to a planter. Boosts growth speed and yield, stacks with fertilizer and modules. Right-click to attach, shift-right-click empty-handed to take it back off. Break a cloched planter and you get both items back separately.

## Machines

- **Composter** — turns organic items into biomass fuel and fertilizer. Anything that works in a vanilla composter works in this one too
- **Biomass Burner** — burns crude/regular/compacted biomass for FE
- **Energy Capacitors** — three tiers, 500k / 1M / 4M FE

## Modules

- **Speed (MK1-3)** — faster processing, costs more power
- **Yield (MK1-3)** — more harvest, slower processing

## Other Blocks

- **Mulch** — +50% growth speed
- **Infused Farmland** — +100% growth speed

## Fertilizer

Bone meal works. Also supports Mystical Agriculture, Immersive Engineering, and Forbidden & Arcanus fertilizers. Pipe or hopper it into any side of either planter.

## Data-Driven

Nothing's hardcoded. Add/remove/edit seeds, soils, saplings, fertilizers through:

- **Datapacks** — regular JSON
- **KubeJS** — for scripted/server-side control

Means you can add support for unsupported mods, change growth modifiers, mess with fertilizer values, or override defaults you don't like.

## Mod Support

**Crops:** Mystical Agriculture & Mystical Agradditions, Farmer's Delight, Ars Nouveau, Silent Gear, Immersive Engineering, Occultism, Cobblemon, Pam's HarvestCraft 2, Actually Additions, Croptopia, The Aether II

**Trees:** Ars Nouveau & Ars Elemental, Forbidden & Arcanus, Integrated Dynamics, Silent Gear, Occultism, Cobblemon, Pam's HarvestCraft 2, Croptopia, EvilCraft, The Aether II

**Soils:** Mystical Agriculture farmland, Farmer's Delight soils, Just Dire Things goosoils, The Aether II

## Balance Config

Module strength, power draw, and processing times are all tunable.

## Interactive Placement

- Right-click with seeds/saplings/soil to insert directly
- Right-click with a hoe to till compatible blocks
- Right-click vanilla farmland with mystical essence to convert it

## Power

Runs on FE.

- Advanced Planter: 64 FE/t base
- Composter: 64 FE/t base
- Modules scale power draw with their effect

## JEI

Shows valid crop/soil recipes for the planters.

## Jade

Shows crop/sapling, growth stage and progress, soil type and its bonus, fertilizer slotted (if any), and cloche speed/yield status.

## Heads Up

Agritech: Evolved (ATE) is NOT related to Agritech (AT2). Not an addon, not a successor, doesn't build on it. Two separate mods, similar name, same general idea, zero shared code.

Running both is fine but pointless — datapacks/KubeJS for one won't do anything for the other, different namespaces entirely.