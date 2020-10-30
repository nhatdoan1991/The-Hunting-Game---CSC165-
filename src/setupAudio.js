var JavaPackages = new JavaImporter( 
	Packages.ray.rage.scene.SceneManager, 
	Packages.ray.rage.scene.SceneNode,
	Packages.ray.rage.util.Configuration,
	Packages.ray.audio.AudioManagerFactory,
	Packages.ray.audio.AudioResource,
	Packages.ray.audio.AudioResourceType,
	Packages.ray.audio.IAudioManager,
	Packages.ray.audio.Sound,
	Packages.ray.audio.SoundType
);

with(JavaPackages)
{
	function setupAudio(mygame) {
		configuration = mygame.getEngine().getSceneManager().getConfiguration();
		sfxPath = configuration.valueOf("assets.sounds.path.a1.sfx");
		musicPath = configuration.valueOf("assets.sounds.path.a2.music");
		var clairDeLune, arabesqueNoOne, reverie, scoreSfx, destroySfx, lifeUpSfx;
		audioManager = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");

		if (!audioManager.initialize()) {
			System.out.println("The Audio Manager failed to initialize :(");
			return;
		}

		clairDeLune = audioManager.createAudioResource(musicPath + "clairdelune.wav", AudioResourceType.AUDIO_STREAM);
		arabesqueNoOne = audioManager.createAudioResource(musicPath + "arabesque_no_one.wav",
				AudioResourceType.AUDIO_STREAM);
		reverie = audioManager.createAudioResource(musicPath + "reverie.wav", AudioResourceType.AUDIO_STREAM);
		scoreSfx = audioManager.createAudioResource(sfxPath + "score.wav", AudioResourceType.AUDIO_SAMPLE);
		destroySfx = audioManager.createAudioResource(sfxPath + "destroyed.wav", AudioResourceType.AUDIO_SAMPLE);
		lifeUpSfx = audioManager.createAudioResource(sfxPath + "lifeup.wav", AudioResourceType.AUDIO_SAMPLE);

		music[0] = new Sound(clairDeLune, SoundType.SOUND_MUSIC, 100, false);
		music[1] = new Sound(arabesqueNoOne, SoundType.SOUND_MUSIC, 100, false);
		music[2] = new Sound(reverie, SoundType.SOUND_MUSIC, 100, false);
		sfx[0] = new Sound(scoreSfx, SoundType.SOUND_EFFECT, 25, false);
		sfx[1] = new Sound(destroySfx, SoundType.SOUND_EFFECT, 25, false);
		sfx[2] = new Sound(lifeUpSfx, SoundType.SOUND_EFFECT, 25, false);

		for (m in music) {
			music[m].initialize(audioManager);
		}

		for (s in sfx) {
			sfx[s].initialize(audioManager);
		}

		music[currentSong].play();		
	}
}