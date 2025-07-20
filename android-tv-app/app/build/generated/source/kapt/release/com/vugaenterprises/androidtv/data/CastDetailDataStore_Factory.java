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
public final class CastDetailDataStore_Factory implements Factory<CastDetailDataStore> {
  @Override
  public CastDetailDataStore get() {
    return newInstance();
  }

  public static CastDetailDataStore_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CastDetailDataStore newInstance() {
    return new CastDetailDataStore();
  }

  private static final class InstanceHolder {
    private static final CastDetailDataStore_Factory INSTANCE = new CastDetailDataStore_Factory();
  }
}
