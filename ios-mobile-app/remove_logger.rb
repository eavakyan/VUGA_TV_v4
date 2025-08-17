#!/usr/bin/env ruby

require 'xcodeproj'

project_path = 'Vuga.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Get the main target
target = project.targets.find { |t| t.name == 'Vuga' }

# Remove all ReleaseLogger references
files_to_remove = []
project.files.each do |file|
  if file.path && file.path.include?('ReleaseLogger')
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

# Save the project
project.save

puts "✅ Removed all ReleaseLogger references"