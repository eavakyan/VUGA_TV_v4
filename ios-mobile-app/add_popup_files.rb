#!/usr/bin/env ruby

require 'xcodeproj'

# Open the project
project_path = 'Vuga.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Get the main group
main_group = project.main_group['Vuga']

unless main_group
  puts "Error: Could not find Vuga group"
  exit 1
end

# Find or create groups
models_group = main_group['Models'] || main_group.new_group('Models')
services_group = main_group['Services'] || main_group.new_group('Services')
views_group = main_group['Views'] || main_group.new_group('Views')

# Get the main target
target = project.targets.find { |t| t.name == 'Vuga' }
unless target
  puts "Error: Could not find Vuga target"
  exit 1
end

# Files to add
files_to_add = [
  { path: 'Vuga/Models/Popup.swift', group: models_group },
  { path: 'Vuga/Services/PopupService.swift', group: services_group },
  { path: 'Vuga/Views/PopupView.swift', group: views_group }
]

# Add each file
files_to_add.each do |file_info|
  file_path = file_info[:path]
  group = file_info[:group]
  
  # Check if file already exists in project
  existing_ref = group.files.find { |f| f.path == File.basename(file_path) }
  
  if existing_ref
    puts "File already exists in project: #{file_path}"
  else
    # Add file reference to group
    file_ref = group.new_file(file_path)
    
    # Add to target's build phase
    target.add_file_references([file_ref])
    
    puts "Added file: #{file_path}"
  end
end

# Save the project
project.save

puts "Project saved successfully!"