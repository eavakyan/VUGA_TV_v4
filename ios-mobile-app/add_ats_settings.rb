#!/usr/bin/env ruby

require 'plist'

# Read the Info.plist
plist_path = 'Vuga/Info.plist'
plist = Plist.parse_xml(plist_path)

# Add App Transport Security settings
plist['NSAppTransportSecurity'] = {
  'NSAllowsArbitraryLoads' => true,
  'NSExceptionDomains' => {
    'iosdev.gossip-stone.com' => {
      'NSExceptionAllowsInsecureHTTPLoads' => true,
      'NSIncludesSubdomains' => true,
      'NSExceptionRequiresForwardSecrecy' => false
    },
    'localhost' => {
      'NSExceptionAllowsInsecureHTTPLoads' => true
    }
  }
}

# Save the plist
File.open(plist_path, 'w') { |f| f.write(plist.to_plist) }

puts "Added App Transport Security settings to Info.plist"