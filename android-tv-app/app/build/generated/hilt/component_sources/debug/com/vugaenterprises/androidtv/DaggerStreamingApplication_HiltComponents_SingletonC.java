package com.vugaenterprises.androidtv;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.vugaenterprises.androidtv.data.CastDetailDataStore;
import com.vugaenterprises.androidtv.data.EpisodeDataStore;
import com.vugaenterprises.androidtv.data.UserDataStore;
import com.vugaenterprises.androidtv.data.VideoPlayerDataStore;
import com.vugaenterprises.androidtv.data.api.ApiService;
import com.vugaenterprises.androidtv.data.repository.ContentRepository;
import com.vugaenterprises.androidtv.data.repository.LiveTVRepository;
import com.vugaenterprises.androidtv.data.repository.ProfileRepository;
import com.vugaenterprises.androidtv.data.repository.TVAuthRepository;
import com.vugaenterprises.androidtv.di.ErrorModule;
import com.vugaenterprises.androidtv.di.ErrorModule_ProvideErrorLoggerFactory;
import com.vugaenterprises.androidtv.di.NetworkModule;
import com.vugaenterprises.androidtv.di.NetworkModule_ProvideApiServiceFactory;
import com.vugaenterprises.androidtv.di.NetworkModule_ProvideOkHttpClientFactory;
import com.vugaenterprises.androidtv.di.NetworkModule_ProvideRetrofitFactory;
import com.vugaenterprises.androidtv.ui.screens.TVAuthDebugViewModel;
import com.vugaenterprises.androidtv.ui.screens.TVAuthDebugViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.AgeSettingsViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.AgeSettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.FavoritesViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.FavoritesViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.HistoryViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.HistoryViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.LiveTVViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.LiveTVViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.ProfileSelectionViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.ProfileSelectionViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.ProfileViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.ProfileViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.QRCodeAuthViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.QRCodeAuthViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.ui.viewmodels.SearchViewModel;
import com.vugaenterprises.androidtv.ui.viewmodels.SearchViewModel_HiltModules_KeyModule_ProvideFactory;
import com.vugaenterprises.androidtv.utils.ErrorLogger;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerStreamingApplication_HiltComponents_SingletonC {
  private DaggerStreamingApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder errorModule(ErrorModule errorModule) {
      Preconditions.checkNotNull(errorModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule(
        HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule) {
      Preconditions.checkNotNull(hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder networkModule(NetworkModule networkModule) {
      Preconditions.checkNotNull(networkModule);
      return this;
    }

    public StreamingApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements StreamingApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public StreamingApplication_HiltComponents.ActivityRetainedC build() {
      return new ActivityRetainedCImpl(singletonCImpl);
    }
  }

  private static final class ActivityCBuilder implements StreamingApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public StreamingApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements StreamingApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public StreamingApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements StreamingApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public StreamingApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements StreamingApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public StreamingApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements StreamingApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public StreamingApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements StreamingApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public StreamingApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends StreamingApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends StreamingApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends StreamingApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends StreamingApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return ImmutableSet.<String>of(AgeSettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ContentDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide(), FavoritesViewModel_HiltModules_KeyModule_ProvideFactory.provide(), HistoryViewModel_HiltModules_KeyModule_ProvideFactory.provide(), HomeViewModel_HiltModules_KeyModule_ProvideFactory.provide(), LiveTVViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ProfileSelectionViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ProfileViewModel_HiltModules_KeyModule_ProvideFactory.provide(), QRCodeAuthViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SearchViewModel_HiltModules_KeyModule_ProvideFactory.provide(), TVAuthDebugViewModel_HiltModules_KeyModule_ProvideFactory.provide());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectVideoPlayerDataStore(instance, singletonCImpl.videoPlayerDataStoreProvider.get());
      MainActivity_MembersInjector.injectEpisodeDataStore(instance, singletonCImpl.episodeDataStoreProvider.get());
      MainActivity_MembersInjector.injectCastDetailDataStore(instance, singletonCImpl.castDetailDataStoreProvider.get());
      MainActivity_MembersInjector.injectUserDataStore(instance, singletonCImpl.userDataStoreProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends StreamingApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AgeSettingsViewModel> ageSettingsViewModelProvider;

    private Provider<ContentDetailViewModel> contentDetailViewModelProvider;

    private Provider<FavoritesViewModel> favoritesViewModelProvider;

    private Provider<HistoryViewModel> historyViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<LiveTVViewModel> liveTVViewModelProvider;

    private Provider<ProfileSelectionViewModel> profileSelectionViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<QRCodeAuthViewModel> qRCodeAuthViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<TVAuthDebugViewModel> tVAuthDebugViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.ageSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.contentDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.favoritesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.historyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.liveTVViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.profileSelectionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.qRCodeAuthViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.tVAuthDebugViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<String, Provider<ViewModel>> getHiltViewModelMap() {
      return ImmutableMap.<String, Provider<ViewModel>>builderWithExpectedSize(11).put("com.vugaenterprises.androidtv.ui.viewmodels.AgeSettingsViewModel", ((Provider) ageSettingsViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel", ((Provider) contentDetailViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.FavoritesViewModel", ((Provider) favoritesViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.HistoryViewModel", ((Provider) historyViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel", ((Provider) homeViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.LiveTVViewModel", ((Provider) liveTVViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.ProfileSelectionViewModel", ((Provider) profileSelectionViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.ProfileViewModel", ((Provider) profileViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.QRCodeAuthViewModel", ((Provider) qRCodeAuthViewModelProvider)).put("com.vugaenterprises.androidtv.ui.viewmodels.SearchViewModel", ((Provider) searchViewModelProvider)).put("com.vugaenterprises.androidtv.ui.screens.TVAuthDebugViewModel", ((Provider) tVAuthDebugViewModelProvider)).build();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.vugaenterprises.androidtv.ui.viewmodels.AgeSettingsViewModel 
          return (T) new AgeSettingsViewModel(singletonCImpl.profileRepositoryProvider.get());

          case 1: // com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel 
          return (T) new ContentDetailViewModel(singletonCImpl.contentRepositoryProvider.get(), singletonCImpl.userDataStoreProvider.get());

          case 2: // com.vugaenterprises.androidtv.ui.viewmodels.FavoritesViewModel 
          return (T) new FavoritesViewModel(singletonCImpl.provideApiServiceProvider.get());

          case 3: // com.vugaenterprises.androidtv.ui.viewmodels.HistoryViewModel 
          return (T) new HistoryViewModel(singletonCImpl.provideApiServiceProvider.get());

          case 4: // com.vugaenterprises.androidtv.ui.viewmodels.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.contentRepositoryProvider.get(), singletonCImpl.userDataStoreProvider.get());

          case 5: // com.vugaenterprises.androidtv.ui.viewmodels.LiveTVViewModel 
          return (T) new LiveTVViewModel(singletonCImpl.liveTVRepositoryProvider.get(), singletonCImpl.userDataStoreProvider.get());

          case 6: // com.vugaenterprises.androidtv.ui.viewmodels.ProfileSelectionViewModel 
          return (T) new ProfileSelectionViewModel(singletonCImpl.profileRepositoryProvider.get(), singletonCImpl.provideErrorLoggerProvider.get());

          case 7: // com.vugaenterprises.androidtv.ui.viewmodels.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.provideApiServiceProvider.get());

          case 8: // com.vugaenterprises.androidtv.ui.viewmodels.QRCodeAuthViewModel 
          return (T) new QRCodeAuthViewModel(singletonCImpl.tVAuthRepositoryProvider.get(), singletonCImpl.provideErrorLoggerProvider.get(), singletonCImpl.userDataStoreProvider.get());

          case 9: // com.vugaenterprises.androidtv.ui.viewmodels.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.contentRepositoryProvider.get());

          case 10: // com.vugaenterprises.androidtv.ui.screens.TVAuthDebugViewModel 
          return (T) new TVAuthDebugViewModel();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends StreamingApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;

      initialize();

    }

    @SuppressWarnings("unchecked")
    private void initialize() {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends StreamingApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends StreamingApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<VideoPlayerDataStore> videoPlayerDataStoreProvider;

    private Provider<EpisodeDataStore> episodeDataStoreProvider;

    private Provider<CastDetailDataStore> castDetailDataStoreProvider;

    private Provider<UserDataStore> userDataStoreProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<ApiService> provideApiServiceProvider;

    private Provider<ProfileRepository> profileRepositoryProvider;

    private Provider<ContentRepository> contentRepositoryProvider;

    private Provider<LiveTVRepository> liveTVRepositoryProvider;

    private Provider<ErrorLogger> provideErrorLoggerProvider;

    private Provider<TVAuthRepository> tVAuthRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.videoPlayerDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<VideoPlayerDataStore>(singletonCImpl, 0));
      this.episodeDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<EpisodeDataStore>(singletonCImpl, 1));
      this.castDetailDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<CastDetailDataStore>(singletonCImpl, 2));
      this.userDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<UserDataStore>(singletonCImpl, 3));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 7));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 6));
      this.provideApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<ApiService>(singletonCImpl, 5));
      this.profileRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ProfileRepository>(singletonCImpl, 4));
      this.contentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ContentRepository>(singletonCImpl, 8));
      this.liveTVRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<LiveTVRepository>(singletonCImpl, 9));
      this.provideErrorLoggerProvider = DoubleCheck.provider(new SwitchingProvider<ErrorLogger>(singletonCImpl, 10));
      this.tVAuthRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<TVAuthRepository>(singletonCImpl, 11));
    }

    @Override
    public void injectStreamingApplication(StreamingApplication streamingApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.vugaenterprises.androidtv.data.VideoPlayerDataStore 
          return (T) new VideoPlayerDataStore();

          case 1: // com.vugaenterprises.androidtv.data.EpisodeDataStore 
          return (T) new EpisodeDataStore();

          case 2: // com.vugaenterprises.androidtv.data.CastDetailDataStore 
          return (T) new CastDetailDataStore();

          case 3: // com.vugaenterprises.androidtv.data.UserDataStore 
          return (T) new UserDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.vugaenterprises.androidtv.data.repository.ProfileRepository 
          return (T) new ProfileRepository(singletonCImpl.provideApiServiceProvider.get(), singletonCImpl.userDataStoreProvider.get());

          case 5: // com.vugaenterprises.androidtv.data.api.ApiService 
          return (T) NetworkModule_ProvideApiServiceFactory.provideApiService(singletonCImpl.provideRetrofitProvider.get());

          case 6: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 7: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 8: // com.vugaenterprises.androidtv.data.repository.ContentRepository 
          return (T) new ContentRepository(singletonCImpl.provideApiServiceProvider.get());

          case 9: // com.vugaenterprises.androidtv.data.repository.LiveTVRepository 
          return (T) new LiveTVRepository(singletonCImpl.provideApiServiceProvider.get());

          case 10: // com.vugaenterprises.androidtv.utils.ErrorLogger 
          return (T) ErrorModule_ProvideErrorLoggerFactory.provideErrorLogger(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideApiServiceProvider.get());

          case 11: // com.vugaenterprises.androidtv.data.repository.TVAuthRepository 
          return (T) new TVAuthRepository(singletonCImpl.provideApiServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
