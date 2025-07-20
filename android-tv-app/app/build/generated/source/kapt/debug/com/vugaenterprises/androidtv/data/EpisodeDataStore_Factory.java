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
public final class EpisodeDataStore_Factory implements Factory<EpisodeDataStore> {
  @Override
  public EpisodeDataStore get() {
    return newInstance();
  }

  public static EpisodeDataStore_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EpisodeDataStore newInstance() {
    return new EpisodeDataStore();
  }

  private static final class InstanceHolder {
    private static final EpisodeDataStore_Factory INSTANCE = new EpisodeDataStore_Factory();
  }
}
