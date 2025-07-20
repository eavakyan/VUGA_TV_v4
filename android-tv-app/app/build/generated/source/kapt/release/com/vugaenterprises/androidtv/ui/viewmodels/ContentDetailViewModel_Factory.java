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
public final class ContentDetailViewModel_Factory implements Factory<ContentDetailViewModel> {
  private final Provider<ContentRepository> contentRepositoryProvider;

  public ContentDetailViewModel_Factory(Provider<ContentRepository> contentRepositoryProvider) {
    this.contentRepositoryProvider = contentRepositoryProvider;
  }

  @Override
  public ContentDetailViewModel get() {
    return newInstance(contentRepositoryProvider.get());
  }

  public static ContentDetailViewModel_Factory create(
      Provider<ContentRepository> contentRepositoryProvider) {
    return new ContentDetailViewModel_Factory(contentRepositoryProvider);
  }

  public static ContentDetailViewModel newInstance(ContentRepository contentRepository) {
    return new ContentDetailViewModel(contentRepository);
  }
}
