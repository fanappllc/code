//
//  LocationTracker.swift
//  FaslaCustomer
//
//  Created by Codiant on 4/20/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import Foundation
import SocketIO

enum SIOSocketState {
  case connected, connecting, disconnected
}

class LocationTracker: NSObject {
  
  var onNewLocation: (([Any]?) -> Void)?
  
  var socket: SocketIOClient?
  private var manager: SocketManager?
  private var orderId: String?
  private var state:SocketIOStatus = .disconnected
  private var observerContext = false
  
    static let shared: LocationTracker = {
        let instance = LocationTracker()
        return instance
    }()
    
    public func startWithOrder(id: String) -> Void {
        
        if state != .disconnected {
            return
        }
        
        orderId = id
        
        if !observerContext {
            observerContext = true
            appDelegate.addObserver(self, forKeyPath: "networkReachable", options: [.new, .old], context: nil)
        }
        
        
        state = .connecting
        // Local - http://prortc.com:6018
        //Codiant Dev - http://fan-socket.codiantdev.com/
        // Live - http://fanapp.us:6018
        manager = SocketManager(socketURL: URL(string: "http://fanapp.us:6018")!, config: [.log(true), .reconnectAttempts(0)])
        socket = manager?.defaultSocket
            
            if self.state == .disconnected {
                socket?.disconnect()
                //socketClient?.close()
                return
            }
            
            if let connectedSCK = socket {
                
                self.socket = connectedSCK
                
                socket?.on(clientEvent: .connect, callback: { data, args in
                    DDLogDebug("socket connected")
                    
                    self.state = .connected
                    
                    connectedSCK.on("new_location", callback: { data, args in
                        if let callback = self.onNewLocation {
                            callback(data)
                        }
                    })
                    self.socket?.emit("start_tracking", ["orderId": self.orderId!])
                })
                
                socket?.on(clientEvent: .disconnect, callback: { data, args in

                    self.state = .disconnected
                })
                
                socket?.on(clientEvent: .error, callback: { data, args in
                    if let error = data.first as? String {
                        
                        DDLogError("Socket error \(error)")
                    }
                    self.state = .disconnected

                })
                socket?.connect()
            }
//        })
    }

  public func sendLocation(args: [Any]?) -> Void {
    
    if state != .connected {
      DDLogDebug("socket is not connected, failed to send location.")
      return
    }
    
    socket?.emit("update_location", with: args!)

  }
  
  public func stop() -> Void {
    
    if observerContext {
      appDelegate.removeObserver(self, forKeyPath: "networkReachable", context: nil)
      observerContext = false
    }
    
    orderId = nil
    
    clear()
  }
  
  public func reconnectIfNeeded() -> Void {
    
    if state == .connected || state == .connecting || orderId == nil {
      return
    }
    
    clear()
    startWithOrder(id: orderId!)
  }
  
  private func clear() -> Void {
    
    //socket?.close()
    socket?.disconnect()
    socket = nil
    state = .disconnected
    manager?.disconnect()
    manager = nil
  }
  
  override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
    
    if let isReachable = change?[.newKey] as? Bool {
      
      if isReachable {
        clear()
        startWithOrder(id: orderId!)
      }
      else {
        clear()
      }
    }
  }
}
