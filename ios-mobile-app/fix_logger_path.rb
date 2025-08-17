#!/usr/bin/env ruby

require 'xcodeproj'

# Open the project
project_path = 'Vuga.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Get the main target
target = project.targets.find { |t| t.name == 'Vuga' }

# Remove incorrect ReleaseLogger references
files_to_remove = []
project.files.each do |file|
  if file.path && file.path.include?('ReleaseLogger.swift')
    files_to_remove << file
    puts "Removing: #{file.path}"
  end
end

# Remove from build phases
if target
  target.source_build_phase.files.delete_if do |build_file|
    file_ref = build_file.file_ref
    if file_ref && file_ref.path
      file_ref.path.include?('ReleaseLogger')
    else
      false
    end
  end
end

# Remove file references
files_to_remove.each do |file|
  file.remove_from_project
end

# Save first
project.save

# Reload and add correct reference
project = Xcodeproj::Project.open(project_path)
target = project.targets.find { |t| t.name == 'Vuga' }
main_group = project.main_group['Vuga']
utilities_group = main_group['Utilities'] || main_group.new_group('Utilities')

# Add with correct path
file_ref = utilities_group.new_reference('Vuga/Utilities/ReleaseLogger.swift')
file_ref.name = 'ReleaseLogger.swift'
target.add_file_references([file_ref])

# Save the project
project.save

puts "✅ Fixed ReleaseLogger.swift path reference"