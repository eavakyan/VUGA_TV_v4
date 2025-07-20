package com.vugaenterprises.androidtv;

import com.vugaenterprises.androidtv.data.CastDetailDataStore;
import com.vugaenterprises.androidtv.data.EpisodeDataStore;
import com.vugaenterprises.androidtv.data.VideoPlayerDataStore;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<VideoPlayerDataStore> videoPlayerDataStoreProvider;

  private final Provider<EpisodeDataStore> episodeDataStoreProvider;

  private final Provider<CastDetailDataStore> castDetailDataStoreProvider;

  public MainActivity_MembersInjector(Provider<VideoPlayerDataStore> videoPlayerDataStoreProvider,
      Provider<EpisodeDataStore> episodeDataStoreProvider,
      Provider<CastDetailDataStore> castDetailDataStoreProvider) {
    this.videoPlayerDataStoreProvider = videoPlayerDataStoreProvider;
    this.episodeDataStoreProvider = episodeDataStoreProvider;
    this.castDetailDataStoreProvider = castDetailDataStoreProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<VideoPlayerDataStore> videoPlayerDataStoreProvider,
      Provider<EpisodeDataStore> episodeDataStoreProvider,
      Provider<CastDetailDataStore> castDetailDataStoreProvider) {
    return new MainActivity_MembersInjector(videoPlayerDataStoreProvider, episodeDataStoreProvider, castDetailDataStoreProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectVideoPlayerDataStore(instance, videoPlayerDataStoreProvider.get());
    injectEpisodeDataStore(instance, episodeDataStoreProvider.get());
    injectCastDetailDataStore(instance, castDetailDataStoreProvider.get());
  }

  @InjectedFieldSignature("com.vugaenterprises.androidtv.MainActivity.videoPlayerDataStore")
  public static void injectVideoPlayerDataStore(MainActivity instance,
      VideoPlayerDataStore videoPlayerDataStore) {
    instance.videoPlayerDataStore = videoPlayerDataStore;
  }

  @InjectedFieldSignature("com.vugaenterprises.androidtv.MainActivity.episodeDataStore")
  public static void injectEpisodeDataStore(MainActivity instance,
      EpisodeDataStore episodeDataStore) {
    instance.episodeDataStore = episodeDataStore;
  }

  @InjectedFieldSignature("com.vugaenterprises.androidtv.MainActivity.castDetailDataStore")
  public static void injectCastDetailDataStore(MainActivity instance,
      CastDetailDataStore castDetailDataStore) {
    instance.castDetailDataStore = castDetailDataStore;
  }
}
