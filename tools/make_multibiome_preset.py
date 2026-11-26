#!/usr/bin/env python3
"""
Multi-biome world preset generator.

  python3 make_multibiome_preset.py <name> <main_biome> [extra_biome ...] [--out DIR]

The main biome gets a larger share of the map (weight 2), every extra biome weight 1.
Without --out the preset is written into the mod resources (src/main/resources/data/tvtime/worldgen/world_preset).
With --out DIR it writes a ready-to-use datapack structure into DIR
  (data/<ns>/worldgen/world_preset/<name>.json + tag), so players can add their own combos without a mod rebuild.

Called without arguments it regenerates every built-in preset of the mod.
"""
import json, os, sys
from statistics import NormalDist

HERE = os.path.dirname(os.path.abspath(__file__))
MOD_RES = os.path.join(HERE, "..", "src", "main", "resources")
ND = NormalDist(0, 0.38)  # climate noises cluster around 0, so cut by quantiles to get roughly equal areas

PRESETS = {
    "desert_multi": ["desert", "badlands", "eroded_badlands", "wooded_badlands", "savanna", "windswept_savanna", "savanna_plateau"],
    "forest_multi": ["forest", "birch_forest", "dark_forest", "flower_forest", "old_growth_birch_forest", "taiga", "cherry_grove", "meadow"],
    "snow_multi": ["snowy_plains", "ice_spikes", "snowy_taiga", "grove", "snowy_slopes", "frozen_peaks", "jagged_peaks"],
    "jungle_multi": ["jungle", "sparse_jungle", "bamboo_jungle", "swamp", "mangrove_swamp", "beach", "river"],
    "mountain_multi": ["windswept_hills", "windswept_forest", "windswept_gravelly_hills", "meadow", "stony_peaks", "jagged_peaks", "frozen_peaks", "snowy_slopes", "grove"],
    "ocean_multi": ["ocean", "warm_ocean", "lukewarm_ocean", "cold_ocean", "frozen_ocean", "deep_ocean", "beach", "stony_shore"],
    "all_land_multi": ["plains", "sunflower_plains", "forest", "birch_forest", "dark_forest", "taiga", "snowy_plains", "ice_spikes", "desert", "savanna",
                       "badlands", "eroded_badlands", "jungle", "bamboo_jungle", "swamp", "mangrove_swamp", "meadow", "cherry_grove",
                       "windswept_hills", "mushroom_fields", "stony_peaks", "flower_forest", "snowy_taiga", "old_growth_pine_taiga"],
}

def edges(n):
    """n+1 edges splitting the climate range into n equal-probability slices."""
    e = [-2.0] + [round(ND.inv_cdf(k / n), 4) for k in range(1, n)] + [2.0]
    return e

def build_biomes(biomes):
    n = len(biomes)
    weights = [2] + [1] * (n - 1)                       # first = main biome
    # lay out in a 2-D grid of (temperature x humidity) cells; main biome takes two cells
    cells = []
    for b, w in zip(biomes, weights): cells += [b] * w
    cols = max(1, round(len(cells) ** 0.5)); rows = -(-len(cells) // cols)
    te, he = edges(cols), edges(rows)
    out = []
    for i, b in enumerate(cells):
        r, c = divmod(i, cols)
        if r % 2: c = cols - 1 - c                       # snake order keeps neighbours related
        out.append({"biome": b if ":" in b else "minecraft:" + b, "parameters": {
            "temperature": [te[c], te[c + 1]], "humidity": [he[r], he[r + 1]],
            "continentalness": [-2.0, 2.0], "erosion": [-2.0, 2.0], "weirdness": [-2.0, 2.0],
            "depth": [0.0, 0.0], "offset": 0.0}})
    return out

def preset(biomes):
    return {"dimensions": {
        "minecraft:overworld": {"type": "minecraft:overworld", "generator": {"type": "minecraft:noise", "settings": "minecraft:overworld",
            "biome_source": {"type": "minecraft:multi_noise", "biomes": build_biomes(biomes)}}},
        "minecraft:the_nether": {"type": "minecraft:the_nether", "generator": {"type": "minecraft:noise", "settings": "minecraft:nether",
            "biome_source": {"type": "minecraft:multi_noise", "preset": "minecraft:nether"}}},
        "minecraft:the_end": {"type": "minecraft:the_end", "generator": {"type": "minecraft:noise", "settings": "minecraft:end",
            "biome_source": {"type": "minecraft:the_end"}}}}}

def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(data, f, indent=2)

def main(argv):
    if not argv:
        for name, biomes in PRESETS.items():
            write(os.path.join(MOD_RES, "data", "tvtime", "worldgen", "world_preset", name + ".json"), preset(biomes))
        write(os.path.join(MOD_RES, "data", "minecraft", "tags", "worldgen", "world_preset", "normal.json"),
              {"values": ["tvtime:" + n for n in PRESETS]})
        print("built-in presets:", ", ".join(PRESETS)); return
    out = None
    if "--out" in argv:
        i = argv.index("--out"); out = argv[i + 1]; del argv[i:i + 2]
    name, biomes = argv[0], argv[1:]
    if not biomes: sys.exit("need at least the main biome")
    ns = "custom"
    base = out or os.path.join(MOD_RES)
    write(os.path.join(base, "data", ns if out else "tvtime", "worldgen", "world_preset", name + ".json"), preset(biomes))
    if out:   # tags from several packs are merged, so "normal.json" is safe in a datapack
        write(os.path.join(out, "data", "minecraft", "tags", "worldgen", "world_preset", "normal.json"), {"values": [ns + ":" + name]})
        write(os.path.join(out, "pack.mcmeta"), {"pack": {"description": "Multi-biome preset " + name, "min_format": 0, "max_format": 999}})
    print("written", name, biomes)

main(sys.argv[1:])
