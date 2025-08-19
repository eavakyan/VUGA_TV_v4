#\!/usr/bin/env ruby

require 'xcodeproj'

# Open the Pods project
project_path = 'Pods/Pods.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Set the development team for all targets
project.targets.each do |target|
  target.build_configurations.each do |config|
    config.build_settings['DEVELOPMENT_TEAM'] = '39Z92MFKA3'
    config.build_settings['CODE_SIGN_IDENTITY'] = ''
    config.build_settings['CODE_SIGNING_REQUIRED'] = 'NO'
    config.build_settings['CODE_SIGNING_ALLOWED'] = 'NO'
  end
end

# Save the project
project.save

puts "Fixed Pods signing settings"
