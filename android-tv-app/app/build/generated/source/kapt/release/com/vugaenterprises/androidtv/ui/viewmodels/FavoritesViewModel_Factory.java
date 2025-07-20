package com.vugaenterprises.androidtv.ui.viewmodels;

import com.vugaenterprises.androidtv.data.api.ApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class FavoritesViewModel_Factory implements Factory<FavoritesViewModel> {
  private final Provider<ApiService> apiServiceProvider;

  public FavoritesViewModel_Factory(Provider<ApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public FavoritesViewModel get() {
    return newInstance(apiServiceProvider.get());
  }

  public static FavoritesViewModel_Factory create(Provider<ApiService> apiServiceProvider) {
    return new FavoritesViewModel_Factory(apiServiceProvider);
  }

  public static FavoritesViewModel newInstance(ApiService apiService) {
    return new FavoritesViewModel(apiService);
  }
}
