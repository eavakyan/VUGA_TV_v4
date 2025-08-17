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

# Remove all popup file references from the project
files_to_remove = []
project.files.each do |file|
  if file.path && (file.path.include?('Popup.swift') || 
                   file.path.include?('PopupService.swift') || 
                   file.path.include?('PopupView.swift'))
    files_to_remove << file
    puts "Found file to remove: #{file.path}"
  end
end

# Remove from build phases
target.source_build_phase.files.delete_if do |build_file|
  file_ref = build_file.file_ref
  if file_ref && file_ref.path
    should_remove = file_ref.path.include?('Popup') || 
                   file_ref.path.include?('PopupService') || 
                   file_ref.path.include?('PopupView')
    puts "Removing from build phase: #{file_ref.path}" if should_remove
    should_remove
  else
    false
  end
end

# Remove file references
files_to_remove.each do |file|
  file.remove_from_project
end

# Save the project
project.save

puts "\n✅ Removed #{files_to_remove.count} popup file references"
puts "Project cleaned. Now adding correct references..."

# Reload project
project = Xcodeproj::Project.open(project_path)
target = project.targets.find { |t| t.name == 'Vuga' }

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

# Add files with correct paths (relative to project root)
files_to_add = [
  { file: 'Vuga/Models/Popup.swift', group: models_group, name: 'Popup.swift' },
  { file: 'Vuga/Services/PopupService.swift', group: services_group, name: 'PopupService.swift' },
  { file: 'Vuga/Views/PopupView.swift', group: views_group, name: 'PopupView.swift' }
]

# Add each file
files_to_add.each do |file_info|
  # Create file reference with explicit path
  file_ref = file_info[:group].new_reference(file_info[:file])
  file_ref.name = file_info[:name]
  
  # Add to build phase
  target.add_file_references([file_ref])
  
  puts "✅ Added: #{file_info[:file]}"
end

# Save the project
project.save

puts "\n✅ Successfully fixed popup file references!"
puts "Please try building the project now."