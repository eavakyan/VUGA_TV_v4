#!/usr/bin/env ruby

require 'xcodeproj'
require 'set'

project_path = 'Vuga.xcodeproj'
project = Xcodeproj::Project.open(project_path)

puts "Loaded project: #{project_path}"

# Collect all Swift Package references
all_package_refs = project.objects.select { |o| o.isa == 'XCRemoteSwiftPackageReference' }
puts "Found #{all_package_refs.size} XCRemoteSwiftPackageReference objects"

# Group references by repository URL
refs_by_url = {}
all_package_refs.each do |ref|
  url = ref.respond_to?(:repositoryURL) ? ref.repositoryURL : nil
  next if url.nil?
  refs_by_url[url] ||= []
  refs_by_url[url] << ref
end

puts "Package groups by URL: #{refs_by_url.keys.size} unique URLs"

# Determine survivor reference per URL, prefer one already listed in package_references
current_refs = project.root_object.package_references.to_a
survivor_by_url = {}

refs_by_url.each do |url, refs|
  preferred = (refs & current_refs).first || refs.first
  survivor_by_url[url] = preferred
end

# Repoint all XCSwiftPackageProductDependency objects to survivor references
deps = project.objects.select { |o| o.isa == 'XCSwiftPackageProductDependency' }
repointed = 0
deps.each do |dep|
  pkg = dep.package
  next if pkg.nil?
  url = pkg.respond_to?(:repositoryURL) ? pkg.repositoryURL : nil
  next if url.nil?
  survivor = survivor_by_url[url]
  next if survivor.nil? || survivor == pkg
  dep.package = survivor
  repointed += 1
end

puts "Repointed #{repointed} XCSwiftPackageProductDependency objects"

# Clean up PBXProject.package_references to contain one per URL, preserving order
ordered_unique = []
seen = {}
current_refs.each do |ref|
  url = ref.respond_to?(:repositoryURL) ? ref.repositoryURL : nil
  next if url.nil?
  next if seen[url]
  seen[url] = true
  survivor = survivor_by_url[url] || ref
  ordered_unique << survivor
end

# Include any survivors that were not present in current order
(survivor_by_url.values - ordered_unique).each do |ref|
  ordered_unique << ref
end

# Replace package_references content (no direct setter available)
project.root_object.package_references.clear
ordered_unique.each do |ref|
  project.root_object.package_references << ref
end
puts "Set package_references to #{ordered_unique.size} unique entries"

# Optionally remove duplicate package reference objects that are not used anymore
survivor_set = survivor_by_url.values.to_set
removed = 0
all_package_refs.each do |ref|
  next if survivor_set.include?(ref)
  # Only remove if nothing points to it
  still_used = deps.any? { |d| d.package == ref }
  next if still_used
  begin
    project.objects.delete(ref)
    project.objects_by_uuid.delete(ref.uuid)
    removed += 1
  rescue StandardError => e
    warn "Warning: could not remove ref #{ref.uuid}: #{e.message}"
  end
end
puts "Removed #{removed} duplicate package reference objects"

project.save
puts 'Saved project with deduplicated Swift Package references.'


