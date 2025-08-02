package com.vugaenterprises.androidtv.ui.viewmodels;

import com.vugaenterprises.androidtv.data.UserDataStore;
import com.vugaenterprises.androidtv.data.repository.ContentRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<ContentRepository> contentRepositoryProvider;

  private final Provider<UserDataStore> userDataStoreProvider;

  public HomeViewModel_Factory(Provider<ContentRepository> contentRepositoryProvider,
      Provider<UserDataStore> userDataStoreProvider) {
    this.contentRepositoryProvider = contentRepositoryProvider;
    this.userDataStoreProvider = userDataStoreProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(contentRepositoryProvider.get(), userDataStoreProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<ContentRepository> contentRepositoryProvider,
      Provider<UserDataStore> userDataStoreProvider) {
    return new HomeViewModel_Factory(contentRepositoryProvider, userDataStoreProvider);
  }

  public static HomeViewModel newInstance(ContentRepository contentRepository,
      UserDataStore userDataStore) {
    return new HomeViewModel(contentRepository, userDataStore);
  }
}
