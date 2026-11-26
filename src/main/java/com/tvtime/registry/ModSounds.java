package com.tvtime.registry;

import com.tvtime.TvTimeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

public final class ModSounds {
	public static final SoundEvent DISC_TV_TIME = sound("music_disc.tv_time");
	public static final SoundEvent DISC_TENNA_EX = sound("music_disc.tenna_ex");
	public static final SoundEvent DISC_END_YOU = sound("music_disc.end_you");
	public static final SoundEvent DISC_KAIZO = sound("music_disc.kaizo");
	public static final SoundEvent BOSS_PHASE1 = sound("music.boss.phase1");
	public static final SoundEvent BOSS_PHASE2 = sound("music.boss.phase2");
	public static final SoundEvent BOSS_PHASE3 = sound("music.boss.phase3");

	public static final ResourceKey<JukeboxSong> SONG_TV_TIME = song("tv_time");
	public static final ResourceKey<JukeboxSong> SONG_TENNA_EX = song("tenna_ex");
	public static final ResourceKey<JukeboxSong> SONG_END_YOU = song("end_you");
	public static final ResourceKey<JukeboxSong> SONG_KAIZO = song("kaizo");

	private static SoundEvent sound(String name) {
		var id = TvTimeMod.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}
	private static ResourceKey<JukeboxSong> song(String name) {
		return ResourceKey.create(Registries.JUKEBOX_SONG, TvTimeMod.id(name)); // data-driven: data/tvtime/jukebox_song/*.json
	}
	public static void init() {}
}
