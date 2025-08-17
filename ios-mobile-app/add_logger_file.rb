#!/usr/bin/env ruby

require 'xcodeproj'

# Open the project
project_path = 'Vuga.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Get the main target
target = project.targets.find { |t| t.name == 'Vuga' }
unless target
  puts "Error: Could not find Vuga target"
  exit 1
end

# Get the main group
main_group = project.main_group['Vuga']
unless main_group
  puts "Error: Could not find Vuga group"
  exit 1
end

# Find or create Utilities group
utilities_group = main_group['Utilities'] || main_group.new_group('Utilities')

# Add ReleaseLogger.swift
file_path = 'Vuga/Utilities/ReleaseLogger.swift'

# Check if file already exists in project
existing_ref = utilities_group.files.find { |f| f.path == 'ReleaseLogger.swift' }

unless existing_ref
  # Add file reference to group
  file_ref = utilities_group.new_reference(file_path)
  file_ref.name = 'ReleaseLogger.swift'
  
  # Add to build phase
  target.add_file_references([file_ref])
  
  puts "✅ Added ReleaseLogger.swift to project"
else
  puts "File already exists in project"
end

# Save the project
project.save

puts "Project saved successfully!"