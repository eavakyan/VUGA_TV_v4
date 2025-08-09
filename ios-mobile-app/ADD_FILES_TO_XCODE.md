# Instructions to Add New Files to Xcode Project

The following Swift files have been created but need to be added to the Xcode project:

1. **TVShowsListView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/TVShowsListView.swift`
2. **MoviesListView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/MoviesListView.swift`
3. **LiveTVListView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/LiveTVListView.swift`
4. **NetworkContentView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/NetworkContentView.swift`

## How to Add Files to Xcode:

1. Open your Xcode project
2. In the Project Navigator (left sidebar), right-click on the `Views` folder
3. Select "Add Files to 'Vuga'..."
4. Navigate to the files listed above
5. Select all four files
6. Make sure "Copy items if needed" is unchecked (files are already in the correct location)
7. Make sure your app target is selected in "Add to targets"
8. Click "Add"

## After Adding Files:

Once the files are added to Xcode, update HomeView.swift to use the actual views instead of placeholders:

1. Replace the TV Shows button action with:
   ```swift
   Navigation.pushToSwiftUiView(TVShowsListView())
   ```

2. Replace the Movies button action with:
   ```swift
   Navigation.pushToSwiftUiView(MoviesListView())
   ```

3. Replace the Live TV button action with:
   ```swift
   Navigation.pushToSwiftUiView(LiveTVListView())
   ```

4. Replace the Networks menu actions with:
   ```swift
   Button("MediaTeka") {
       Navigation.pushToSwiftUiView(NetworkContentView(networkName: "MediaTeka"))
   }
   Button("HBO") {
       Navigation.pushToSwiftUiView(NetworkContentView(networkName: "HBO"))
   }
   ```

The app should then build and run successfully with all the new functionality!