#!/usr/bin/env ruby
# SAFE FILE ADDER - Prevents path doubling issues
# Usage: ruby safe_add_file.rb <file_path> <group_name>
# Example: ruby safe_add_file.rb Vuga/Services/NewService.swift Services

require 'xcodeproj'

def add_file_safely(file_path, group_name)
  project = Xcodeproj::Project.open('Vuga.xcodeproj')
  target = project.targets.find { |t| t.name == 'Vuga' }
  
  unless target
    puts "❌ Error: Could not find Vuga target"
    return false
  end
  
  main_group = project.main_group['Vuga']
  unless main_group
    puts "❌ Error: Could not find Vuga group"
    return false
  end
  
  # Find or create the target group
  target_group = main_group[group_name] || main_group.new_group(group_name)
  
  # Extract just the filename
  filename = File.basename(file_path)
  
  # Check if file already exists
  existing = target_group.files.find { |f| f.path == filename || f.path == file_path }
  if existing
    puts "⚠️  File already exists in project: #{filename}"
    return false
  end
  
  # CRITICAL: Use new_file with RELATIVE path from group, not full path
  file_ref = target_group.new_file(filename)
  
  # Set the source tree and full path
  file_ref.source_tree = 'SOURCE_ROOT'
  file_ref.path = file_path
  
  # Add to target
  target.add_file_references([file_ref])
  
  # Save project
  project.save
  
  puts "✅ Successfully added #{filename} to #{group_name} group"
  puts "   Full path: #{file_path}"
  return true
end

# Main execution
if ARGV.length != 2
  puts "Usage: ruby safe_add_file.rb <file_path> <group_name>"
  puts "Example: ruby safe_add_file.rb Vuga/Services/NewService.swift Services"
  exit 1
end

add_file_safely(ARGV[0], ARGV[1])