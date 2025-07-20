package com.vugaenterprises.androidtv.data.repository;

import com.vugaenterprises.androidtv.data.api.ApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ContentRepository_Factory implements Factory<ContentRepository> {
  private final Provider<ApiService> apiServiceProvider;

  public ContentRepository_Factory(Provider<ApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public ContentRepository get() {
    return newInstance(apiServiceProvider.get());
  }

  public static ContentRepository_Factory create(Provider<ApiService> apiServiceProvider) {
    return new ContentRepository_Factory(apiServiceProvider);
  }

  public static ContentRepository newInstance(ApiService apiService) {
    return new ContentRepository(apiService);
  }
}
