#!/usr/bin/env ruby

require 'xcodeproj'

# Open the project
project_path = 'Vuga.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Remove all package references
packages_to_remove = []
project.root_object.package_references.each do |package|
  packages_to_remove << package
end

packages_to_remove.each do |package|
  project.root_object.package_references.delete(package)
end

# Save the project
project.save

puts "Removed #{packages_to_remove.count} package references"
puts "Project saved. Please re-add packages through Xcode."