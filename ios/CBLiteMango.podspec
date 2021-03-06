#
# Be sure to run `pod lib lint CBLiteMango.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'CBLiteMango'
  s.version          = '0.1.0'
  s.summary          = 'A static library to generate CBLite queries from Mango style JSON.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/tommyo/CBLiteMango'
  s.license          = { :type => 'MIT', :file => '../../LICENSE' }
  s.author           = { 'tommyo' => 'tworeilly@gmail.com' }
  s.source           = { :git => 'https://github.com/tommyo/CBLiteMango.git', :tag => s.version.to_s }

  s.ios.deployment_target = '9.0'
  s.swift_version = '3.2'

  s.source_files = 'CBLiteMango/Classes/**/*'
  
  # s.resource_bundles = {
  #   'CBLiteMango' => ['CBLiteMango/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  # s.frameworks = 'UIKit', 'MapKit'
  s.dependency 'CouchbaseLite-Swift', '~> 2.0.0'
end
