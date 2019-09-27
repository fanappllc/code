# Uncomment the next line to define a global platform for your project
 platform :ios, '10.0'

def shared_pods
# Comment the next line if you're not using Swift and don't want to use dynamic frameworks
  use_frameworks!

    pod 'GKActionSheetPicker'
    pod 'CocoaLumberjack/Swift'
    pod 'libPhoneNumber-iOS'
    pod 'GoogleMaps'
    pod 'Stripe'
    pod 'ReachabilitySwift'
    pod 'ObjectMapper', '3.3'
    # Following pods are directly targeting development branch for Swift4 compatibility, remove source when stable release is available for Swift4
    pod 'Kingfisher', :git => 'https://github.com/onevcat/Kingfisher.git', :branch => 'master'
    pod 'IQKeyboardManagerSwift', '5.0.0'
    pod 'Socket.IO-Client-Swift'
    pod 'Siren'
end

target 'FanCustomer' do
  shared_pods
  
  # Pods for FanCustomer
end

target 'FanPhotographer' do
  shared_pods
  # Pods for FanPhotographer
end
