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
public final class HistoryViewModel_Factory implements Factory<HistoryViewModel> {
  private final Provider<ApiService> apiServiceProvider;

  public HistoryViewModel_Factory(Provider<ApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public HistoryViewModel get() {
    return newInstance(apiServiceProvider.get());
  }

  public static HistoryViewModel_Factory create(Provider<ApiService> apiServiceProvider) {
    return new HistoryViewModel_Factory(apiServiceProvider);
  }

  public static HistoryViewModel newInstance(ApiService apiService) {
    return new HistoryViewModel(apiService);
  }
}
