package com.vugaenterprises.androidtv.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class VideoPlayerDataStore_Factory implements Factory<VideoPlayerDataStore> {
  @Override
  public VideoPlayerDataStore get() {
    return newInstance();
  }

  public static VideoPlayerDataStore_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VideoPlayerDataStore newInstance() {
    return new VideoPlayerDataStore();
  }

  private static final class InstanceHolder {
    private static final VideoPlayerDataStore_Factory INSTANCE = new VideoPlayerDataStore_Factory();
  }
}
