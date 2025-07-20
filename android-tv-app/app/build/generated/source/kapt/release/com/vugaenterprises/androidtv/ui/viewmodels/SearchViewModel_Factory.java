package com.vugaenterprises.androidtv.ui.viewmodels;

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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<ContentRepository> contentRepositoryProvider;

  public SearchViewModel_Factory(Provider<ContentRepository> contentRepositoryProvider) {
    this.contentRepositoryProvider = contentRepositoryProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(contentRepositoryProvider.get());
  }

  public static SearchViewModel_Factory create(
      Provider<ContentRepository> contentRepositoryProvider) {
    return new SearchViewModel_Factory(contentRepositoryProvider);
  }

  public static SearchViewModel newInstance(ContentRepository contentRepository) {
    return new SearchViewModel(contentRepository);
  }
}
