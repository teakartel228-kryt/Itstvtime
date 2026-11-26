#!/usr/bin/env python3
"""Writes every JSON resource (models, item definitions, recipes, loot, lang, sounds...)."""
import json, os
RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "tvtime"
def w(path, data):
    p = os.path.join(RES, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8") as f: json.dump(data, f, indent=2, ensure_ascii=False)
A = lambda p: "assets/%s/%s" % (NS, p)
D = lambda p: "data/%s/%s" % (NS, p)

# ---------------------------------------------------------------- sounds
songs = {  # id: (ogg, seconds, ru title, en title)
    "tv_time":   ("tv_time",   165, "Deltarune - IT'S TV TIME!",              "Deltarune - IT'S TV TIME!"),
    "tenna_ex":  ("tenna_ex",  146, "Deltarune - THAT MAKES YOU SCREAM (Tenna EX)", "Deltarune - THAT MAKES YOU SCREAM (Tenna EX)"),
    "end_you":   ("end_you",   137, "Deltarune - It's Time to End You",       "Deltarune - It's Time to End You"),
    "kaizo":     ("kaizo",     157, "Deltarune - KAIZO (Remastered)",         "Deltarune - KAIZO (Remastered)"),
}
sounds = {}
for k, (ogg, *_ ) in songs.items():
    sounds["music_disc." + k] = {"sounds": [{"name": "%s:music/%s" % (NS, ogg), "stream": True}]}
for i, ogg in enumerate(["tv_time", "tenna_ex", "end_you"], 1):     # boss theme per phase (same audio files)
    sounds["music.boss.phase%d" % i] = {"sounds": [{"name": "%s:music/%s" % (NS, ogg), "stream": True, "attenuation_distance": 64}]}
w(A("sounds.json"), sounds)
for k, (ogg, sec, ru, en) in songs.items():
    w(D("jukebox_song/%s.json" % k), {"comparator_output": 15, "length_in_seconds": sec,
        "description": {"translate": "jukebox_song.%s.%s" % (NS, k)}, "sound_event": "%s:music_disc.%s" % (NS, k)})

# ---------------------------------------------------------------- item definitions + models
def simple_item(name, parent="minecraft:item/generated", tex=None):
    w(A("items/%s.json" % name), {"model": {"type": "minecraft:model", "model": "%s:item/%s" % (NS, name)}})
    w(A("models/item/%s.json" % name), {"parent": parent, "textures": {"layer0": "%s:item/%s" % (NS, tex or name)}})
for n in ["tv_scrap", "tv_remote", "tenna_helmet", "tenna_chestplate", "tenna_leggings", "tenna_boots"]: simple_item(n)
for k in songs: simple_item("music_disc_" + k)
# spawn eggs (vanilla template + tint)
def egg(name, c1, c2):
    w(A("items/%s.json" % name), {"model": {"type": "minecraft:model", "model": "minecraft:item/template_spawn_egg",
        "tints": [{"type": "minecraft:constant", "value": c1}, {"type": "minecraft:constant", "value": c2}]}})
egg("smart_zombie_spawn_egg", 0x00AFAF, 0x799C65)
egg("fast_smart_zombie_spawn_egg", 0xFFD628, 0x799C65)
egg("slow_smart_zombie_spawn_egg", 0x555555, 0x799C65)
egg("ant_tenna_spawn_egg", 0xC9202C, 0xFFD628)

# ---------------------------------------------------------------- armor equipment asset
w(A("equipment/tenna.json"), {"layers": {
    "humanoid":          [{"texture": "%s:tenna" % NS}, {"texture": "%s:tenna_overlay" % NS}],
    "humanoid_leggings": [{"texture": "%s:tenna" % NS}, {"texture": "%s:tenna_overlay" % NS}]}})

# ---------------------------------------------------------------- trap block models (custom, element based)
def box(f, t, tex="#t", extra=None):
    e = {"from": f, "to": t, "faces": {s: {"texture": tex} for s in ("north", "south", "east", "west", "up", "down")}}
    if extra: e.update(extra)
    return e
def rot(origin, axis, angle): return {"rotation": {"origin": origin, "axis": axis, "angle": angle}}

# bear trap: open (armed) = base plate, pressure plate, two jaws tilted outwards with teeth
open_el = [box([1, 0, 1], [15, 1, 15]), box([6, 1, 6], [10, 2, 10]),
           box([1, 1, 1], [3, 7, 15], extra=rot([2, 1, 8], "z", -22.5)),
           box([13, 1, 1], [15, 7, 15], extra=rot([14, 1, 8], "z", 22.5))] + \
          [box([x, 5, 2 + 3 * i], [x + 1, 8, 3 + 3 * i], extra=rot([x + .5, 5, 2.5 + 3 * i], "z", -22.5 if x < 8 else 22.5)) for i in range(4) for x in (3.2, 11.8)]
closed_el = [box([1, 0, 1], [15, 1, 15]), box([2, 1, 3], [7, 4, 13]), box([9, 1, 3], [14, 4, 13])] + \
            [box([6.5 + (0 if i % 2 else 1.5), 3, 3.5 + 2.2 * i], [8.5 + (0 if i % 2 else 1.5), 5, 4.5 + 2.2 * i]) for i in range(5)]
common = {"textures": {"t": "%s:block/bear_trap" % NS, "particle": "%s:block/bear_trap" % NS}}
w(A("models/block/bear_trap_open.json"), {**common, "elements": open_el})
w(A("models/block/bear_trap_closed.json"), {**common, "elements": closed_el})
w(A("blockstates/bear_trap.json"), {"variants": {"armed=true": {"model": "%s:block/bear_trap_open" % NS}, "armed=false": {"model": "%s:block/bear_trap_closed" % NS}}})
w(A("items/bear_trap.json"), {"model": {"type": "minecraft:model", "model": "%s:block/bear_trap_open" % NS}})

sp_common = {"textures": {"t": "%s:block/spike_trap" % NS, "particle": "%s:block/spike_trap" % NS}}
plate = [box([0, 0, 0], [16, 2, 16])]
retracted = plate + [box([2 + 4 * i, 2, 2 + 4 * j], [3 + 4 * i, 3, 3 + 4 * j]) for i in range(4) for j in range(4)]
extended = plate + [box([2 + 4 * i, 2, 2 + 4 * j], [3.5 + 4 * i, 10 - ((i + j) % 2) * 2, 3.5 + 4 * j]) for i in range(4) for j in range(4)]
w(A("models/block/spike_trap_retracted.json"), {**sp_common, "elements": retracted})
w(A("models/block/spike_trap_extended.json"), {**sp_common, "elements": extended})
w(A("blockstates/spike_trap.json"), {"variants": {"extended=false": {"model": "%s:block/spike_trap_retracted" % NS}, "extended=true": {"model": "%s:block/spike_trap_extended" % NS}}})
w(A("items/spike_trap.json"), {"model": {"type": "minecraft:model", "model": "%s:block/spike_trap_retracted" % NS}})

# block loot + mining tags
for b in ("bear_trap", "spike_trap"):
    w(D("loot_table/blocks/%s.json" % b), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "%s:%s" % (NS, b)}],
                                      "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
w("data/minecraft/tags/block/mineable/pickaxe.json", {"values": ["%s:bear_trap" % NS, "%s:spike_trap" % NS]})
w("data/minecraft/tags/item/creeper_drop_music_discs.json", {"values": ["%s:music_disc_%s" % (NS, k) for k in songs]})
w(D("tags/item/repairs_tenna_armor.json"), {"values": ["%s:tv_scrap" % NS]})

# ---------------------------------------------------------------- recipes
def shaped(name, pattern, key, result, count=1, cat="equipment"):
    w(D("recipe/%s.json" % name), {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pattern, "key": key, "result": {"id": result, "count": count}})
S = {"S": "%s:tv_scrap" % NS}
shaped("tenna_helmet", ["SSS", "S S"], S, NS + ":tenna_helmet")
shaped("tenna_chestplate", ["S S", "SSS", "SSS"], S, NS + ":tenna_chestplate")
shaped("tenna_leggings", ["SSS", "S S", "S S"], S, NS + ":tenna_leggings")
shaped("tenna_boots", ["S S", "S S"], S, NS + ":tenna_boots")
shaped("bear_trap", ["I I", "ITI", "III"], {"I": "minecraft:iron_ingot", "T": "minecraft:tripwire_hook"}, NS + ":bear_trap", cat="redstone")
shaped("spike_trap", ["BBB", "BPB", "III"], {"B": "minecraft:iron_bars", "P": "minecraft:stone_pressure_plate", "I": "minecraft:iron_ingot"}, NS + ":spike_trap", cat="redstone")
shaped("tv_remote", [" L ", "GRG", " S "], {"L": "minecraft:lever", "G": "minecraft:glass_pane", "R": "minecraft:redstone_block", "S": S["S"]}, NS + ":tv_remote", cat="misc")

# ---------------------------------------------------------------- boss loot
w(D("loot_table/entities/ant_tenna.json"), {"type": "minecraft:entity", "pools": [
    {"rolls": {"type": "minecraft:uniform", "min": 6, "max": 12}, "entries": [{"type": "minecraft:item", "name": NS + ":tv_scrap"}]},
    {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "%s:music_disc_%s" % (NS, k)} for k in songs]},
    {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": 0.35}], "entries": [{"type": "minecraft:item", "name": "minecraft:diamond", "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]}]}]})

# ---------------------------------------------------------------- language
en = {
 "itemGroup.tvtime": "TV Time",
 "item.tvtime.tv_scrap": "TV Scrap", "item.tvtime.tv_remote": "Mysterious Remote",
 "item.tvtime.tenna_helmet": "Tenna's CRT Head", "item.tvtime.tenna_chestplate": "Tenna's Showman Coat",
 "item.tvtime.tenna_leggings": "Tenna's Showman Pants", "item.tvtime.tenna_boots": "Tenna's Dancing Shoes",
 "item.tvtime.music_disc_tv_time": "Music Disc", "item.tvtime.music_disc_tenna_ex": "Music Disc",
 "item.tvtime.music_disc_end_you": "Music Disc", "item.tvtime.music_disc_kaizo": "Music Disc",
 "item.tvtime.smart_zombie_spawn_egg": "Smart Zombie Spawn Egg", "item.tvtime.fast_smart_zombie_spawn_egg": "Fast Smart Zombie Spawn Egg",
 "item.tvtime.slow_smart_zombie_spawn_egg": "Slow Smart Zombie Spawn Egg", "item.tvtime.ant_tenna_spawn_egg": "Mr. Ant-Tenna Spawn Egg",
 "block.tvtime.bear_trap": "Bear Trap", "block.tvtime.spike_trap": "Spike Trap",
 "entity.tvtime.smart_zombie": "Smart Zombie", "entity.tvtime.fast_smart_zombie": "Fast Smart Zombie",
 "entity.tvtime.slow_smart_zombie": "Slow Smart Zombie", "entity.tvtime.ant_tenna": "Mr. Ant-Tenna",
 "tvtime.boss.start": "IT'S!! TV!! TIME!!!", "tvtime.boss.phase2": "Tired of CLASSICS?! Let's go NEO-style!",
 "tvtime.boss.phase3": "FINAL EPISODE! Don't throw me away!!", "tvtime.boss.death": "Is it... time for a new TV?...",
 "tvtime.boss.shrug": "Hm... the studio is empty.", "tvtime.trap.rearmed": "Trap re-armed",
}
ru = dict(en)
ru.update({
 "item.tvtime.tv_scrap": "Обломок ТВ", "item.tvtime.tv_remote": "Загадочный пульт",
 "item.tvtime.tenna_helmet": "ЭЛТ-голова Тенны", "item.tvtime.tenna_chestplate": "Фрак шоумена Тенны",
 "item.tvtime.tenna_leggings": "Штаны шоумена Тенны", "item.tvtime.tenna_boots": "Танцевальные туфли Тенны",
 "item.tvtime.music_disc_tv_time": "Музыкальная пластинка", "item.tvtime.music_disc_tenna_ex": "Музыкальная пластинка",
 "item.tvtime.music_disc_end_you": "Музыкальная пластинка", "item.tvtime.music_disc_kaizo": "Музыкальная пластинка",
 "item.tvtime.smart_zombie_spawn_egg": "Яйцо призыва умного зомби", "item.tvtime.fast_smart_zombie_spawn_egg": "Яйцо призыва быстрого умного зомби",
 "item.tvtime.slow_smart_zombie_spawn_egg": "Яйцо призыва медленного умного зомби", "item.tvtime.ant_tenna_spawn_egg": "Яйцо призыва мистера Ант-Тенны",
 "block.tvtime.bear_trap": "Капкан", "block.tvtime.spike_trap": "Шипованная ловушка",
 "entity.tvtime.smart_zombie": "Умный зомби", "entity.tvtime.fast_smart_zombie": "Быстрый умный зомби",
 "entity.tvtime.slow_smart_zombie": "Медленный умный зомби", "entity.tvtime.ant_tenna": "Мистер Ант-Тенна",
 "tvtime.boss.start": "ПРИШЛО!! ВРЕМЯ!! ТВ!!!", "tvtime.boss.phase2": "Устали от КЛАССИКИ?! Сделаем всё в НЕО-стиле!",
 "tvtime.boss.phase3": "ФИНАЛЬНЫЙ ЭПИЗОД! Не выбрасывайте меня!!", "tvtime.boss.death": "Неужели... пора покупать новый телевизор?...",
 "tvtime.boss.shrug": "Хм... студия опустела.", "tvtime.trap.rearmed": "Ловушка взведена",
})
presets_en = {"desert_multi": "Multi-Biome: Deserts", "forest_multi": "Multi-Biome: Forests", "snow_multi": "Multi-Biome: Snow",
              "jungle_multi": "Multi-Biome: Jungle & Swamp", "mountain_multi": "Multi-Biome: Mountains", "ocean_multi": "Multi-Biome: Oceans",
              "all_land_multi": "Multi-Biome: Everything"}
presets_ru = {"desert_multi": "Мультибиом: Пустыни", "forest_multi": "Мультибиом: Леса", "snow_multi": "Мультибиом: Снега",
              "jungle_multi": "Мультибиом: Джунгли и болота", "mountain_multi": "Мультибиом: Горы", "ocean_multi": "Мультибиом: Океаны",
              "all_land_multi": "Мультибиом: Всё сразу"}
for k, v in presets_en.items(): en["generator.%s.%s" % (NS, k)] = v
for k, v in presets_ru.items(): ru["generator.%s.%s" % (NS, k)] = v
for k, nm in (("smart_zombie", "normal"), ("fast_smart_zombie", "fast"), ("slow_smart_zombie", "slow")):
    en["entity.tvtime." + k] = "Digger Zombie (%s digging)" % nm; en["item.tvtime.%s_spawn_egg" % k] = "Spawn Egg: Digger Zombie (%s digging)" % nm
    r = {"normal": "обычная скорость ломания", "fast": "быстрое ломание", "slow": "медленное ломание"}[nm]
    ru["entity.tvtime." + k] = "Зомби-копатель (%s)" % r; ru["item.tvtime.%s_spawn_egg" % k] = "Яйцо призыва: Зомби-копатель (%s)" % r
for k, (_, _, rutitle, entitle) in songs.items():
    en["jukebox_song.%s.%s" % (NS, k)] = entitle; ru["jukebox_song.%s.%s" % (NS, k)] = rutitle
w(A("lang/en_us.json"), en); w(A("lang/ru_ru.json"), ru)
print("data ok")
