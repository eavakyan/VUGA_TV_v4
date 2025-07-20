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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<ApiService> apiServiceProvider;

  public ProfileViewModel_Factory(Provider<ApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(apiServiceProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<ApiService> apiServiceProvider) {
    return new ProfileViewModel_Factory(apiServiceProvider);
  }

  public static ProfileViewModel newInstance(ApiService apiService) {
    return new ProfileViewModel(apiService);
  }
}
