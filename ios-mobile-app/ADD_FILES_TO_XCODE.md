# Instructions to Add New Files to Xcode Project

The following Swift files have been created but need to be added to the Xcode project:

## Recently Watched API Files

1. **RecentlyWatchedModel.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Models/RecentlyWatchedModel.swift`
2. **RecentlyWatchedViewModel.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/ViewModels/RecentlyWatchedViewModel.swift`

## Previously Added Files (if not already in project)

3. **TVShowsListView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/TVShowsListView.swift`
4. **MoviesListView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/MoviesListView.swift`
5. **LiveTVListView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/LiveTVListView.swift`
6. **NetworkContentView.swift** - Located at: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/NetworkContentView.swift`

## How to Add Files to Xcode:

1. Open your Xcode project
2. In the Project Navigator (left sidebar), right-click on the appropriate folder:
   - For Model files: Right-click on `Models` folder
   - For ViewModel files: Right-click on `ViewModels` folder
   - For View files: Right-click on `Views` folder
3. Select "Add Files to 'Vuga'..."
4. Navigate to the files listed above
5. Select the files you need to add
6. Make sure "Copy items if needed" is unchecked (files are already in the correct location)
7. Make sure your app target is selected in "Add to targets"
8. Click "Add"

## Build After Adding Files

Once you've added the files, try building the project again. The build errors related to missing types should be resolved.

## Note on HomeView

The HomeView.swift file has been updated to include the RecentlyWatchedAPICard component directly at the bottom of the file as a workaround. This allows the Recently Watched functionality to work without requiring a separate file.