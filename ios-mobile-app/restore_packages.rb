#!/usr/bin/env ruby

require 'xcodeproj'
require 'securerandom'

# Open the project
project_path = 'Vuga.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Define packages to add
packages = [
  { url: 'https://github.com/Alamofire/Alamofire', version: '5.10.2' },
  { url: 'https://github.com/firebase/firebase-ios-sdk.git', version: '11.15.0' },
  { url: 'https://github.com/BranchMetrics/ios-branch-deep-linking', version: '3.12.2' },
  { url: 'https://github.com/google/GoogleSignIn-iOS', version: '7.1.0' },
  { url: 'https://github.com/googleads/swift-package-manager-google-mobile-ads.git', version: '11.13.0' },
  { url: 'https://github.com/googleads/swift-package-manager-google-user-messaging-platform.git', version: '2.7.0' },
  { url: 'https://github.com/onevcat/Kingfisher', version: '7.12.0' },
  { url: 'https://github.com/airbnb/lottie-ios', version: '4.5.2' },
  { url: 'https://github.com/RevenueCat/purchases-ios.git', version: '4.43.7' },
  { url: 'https://github.com/exyte/ScalingHeaderScrollView', version: '1.1.7' },
  { url: 'https://github.com/spacenation/swiftui-sliders', version: '2.1.0' },
  { url: 'https://github.com/markiv/SwiftUI-Shimmer', version: '1.5.1' },
  { url: 'https://github.com/stonko1994/Marquee', version: '1.0.1' },
  { url: 'https://github.com/diniska/swiftui-wrapping-stack', version: '1.1.0' },
  { url: 'https://github.com/n3d1117/ExpandableText', branch: 'main' },
  { url: 'https://github.com/wxxsw/VideoPlayer', version: '1.2.5' },
  { url: 'https://github.com/dagronf/SwiftSubtitles', version: '1.9.0' },
  { url: 'https://github.com/SvenTiigi/YouTubePlayerKit.git', version: '1.9.0' },
  { url: 'https://github.com/tevelee/SwiftUI-Flow', version: '1.7.0' }
]

# Add each package
packages.each do |pkg|
  # Create package reference
  package_ref = project.new(Xcodeproj::Project::Object::XCRemoteSwiftPackageReference)
  package_ref.repositoryURL = pkg[:url]
  
  if pkg[:version]
    package_ref.requirement = {
      'kind' => 'upToNextMajorVersion',
      'minimumVersion' => pkg[:version]
    }
  elsif pkg[:branch]
    package_ref.requirement = {
      'kind' => 'branch',
      'branch' => pkg[:branch]
    }
  end
  
  # Add to project
  project.root_object.package_references << package_ref
  
  puts "Added package: #{pkg[:url]}"
end

# Save the project
project.save

puts "\nRestored #{packages.count} package references"
puts "Project saved successfully!"