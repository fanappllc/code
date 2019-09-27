//
//  StartShootVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/30/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import UserNotifications
import AVFoundation

class StartShootVC: UIViewController {
    var countdownTimer : Timer?
    @IBOutlet weak var lblTimer : UILabel!
    @IBOutlet weak var lblInfo  : UILabel!
    @IBOutlet weak var viewCamera : UIView!
    @IBOutlet weak var countdownTimerView: SRCountdownTimer!
    
    var captureSession: AVCaptureSession?
    var videoPreviewLayer: AVCaptureVideoPreviewLayer?
    var capturePhotoOutput: AVCapturePhotoOutput?
    let cameraView = UIView()
    var order = Order()
    var seconds = 0
    var totalSeconds : Int = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        totalSeconds = TimerOperation.totalTime
        if TimerOperation.remainingTime > 0 {
            registerNotifications()
            checkNotifications()
        } else {
            // Get received notification and check for renew session notification
            sessionCompleted()
        }
        self.lblInfo.text = "Session will end after \(TimeInterval(order.duration)!.getMinutesIntoHoursDay()), or user can end it anytime"
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if TimerOperation.remainingTime > 0 {
            startTimerOperation()
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopTimer()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    //  MARK:- Register Notification
    func registerNotifications() {
        NotificationCenter.default.addObserver(self, selector: #selector(self.orderSessionOperation), name: NSNotification.Name(rawValue: "ORDER_SESSION_OPERATION"), object: nil)
        NotificationCenter .default .addObserver(self, selector: #selector(willResignActive), name: NSNotification.Name.UIApplicationWillResignActive, object: nil)
        NotificationCenter .default .addObserver(self, selector: #selector(willTerminate), name: NSNotification.Name.UIApplicationWillTerminate, object: nil)
        NotificationCenter .default .addObserver(self, selector: #selector(didBecomeActive), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
        NotificationCenter .default .addObserver(self, selector: #selector(didEnterBackground), name: NSNotification.Name.UIApplicationDidEnterBackground, object: nil)
    }
    
    //  MARK:- Notification observer
    @objc func willResignActive() {
        // Save your settings
        TimerOperation.shared.saveTimeOperation(isTimerRunning: true, remainingTime: seconds)
    }
    
    @objc func didEnterBackground() {
        // Save your settings
        TimerOperation.shared.saveTimeOperation(isTimerRunning: true, remainingTime: seconds)
    }
    
    @objc func willTerminate() {
        // Save your settings
        TimerOperation.shared.saveTimeOperation(isTimerRunning: true, remainingTime: seconds)
    }
    
    func checkNotifications()  {
        UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
            DispatchQueue.main.sync { /* or .async {} */
                // update UI
                for notification in notifications {
                    let userInfo = notification.request.content.userInfo
                    if let currentOrder = userInfo["order_id"] as? String, self.order.id! == currentOrder {
                        if userInfo["type"] as? String == "end_session" || userInfo["type"] as? String == "renew_session_time"  {
                            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "ORDER_SESSION_OPERATION"), object: nil, userInfo: userInfo)
                            break
                        }
                    }
                }
                let center = UNUserNotificationCenter.current()
                center.removeAllDeliveredNotifications()
            }
        }
    }
    
    @objc func didBecomeActive() {
        // Retrive your settings
        if TimerOperation.remainingTime > 0 {
            startTimerOperation()
            checkNotifications()
        } else {
            stopTimer()
            sessionCompleted()
        }
    }
    
    @objc private func orderSessionOperation(_ notification: NSNotification) {
        if let userInfo = notification.userInfo {
            if let notificationType = userInfo["type"] as? String {
                switch notificationType {
                case "renew_session_time" :
                    dismissCamera()
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                        let elapsedSec = TimerOperation.totalTime - self.seconds
                        self.totalSeconds = TimerOperation.totalTime + (Int(userInfo["slot_time"] as! String)! * 60 )
                        TimerOperation.totalTime = self.totalSeconds
                        self.countdownTimerView.end()
                        self.countdownTimerView.start(beginingValue: self.totalSeconds, interval: 1, elapsedInterval: TimeInterval(elapsedSec))
                        self.seconds = self.totalSeconds - elapsedSec
                        
                        let localModel  = self.order
                        var price = Int((localModel.amount)!)!
                        price += Int((userInfo["slot_price"] as? String)!)!
                        localModel.amount = (price as NSNumber).stringValue
                        var duration = Int((localModel.duration)!)!
                        duration += Int((userInfo["slot_time"] as? String)!)!
                        localModel.duration = (duration as NSNumber).stringValue
                        Order.currentOrder  = localModel
                        self.lblInfo.text = "Session will end after \(TimeInterval(self.order.duration)!.getMinutesIntoHoursDay()), or user can end it anytime"
                    }
                    break
                case "end_session" :
                    sessionCompleted()
                    break
                default:
                    break
                }
            }
        }
    }
    
    //  MARK:- Private methods
    private func sessionCompleted() {
        NotificationCenter.default.removeObserver(self)
        dismissCamera()
        if let billDetailVC = self.storyboard?.viewController(withClass: BillDetailVC.self) {
            billDetailVC.order = order
            self.push(billDetailVC)
        }
    }
    
    @objc func capture(_ sender: UIButton) {
        // Make sure capturePhotoOutput is valid
        guard let capturePhotoOutput = self.capturePhotoOutput else { return }
        
        // Get an instance of AVCapturePhotoSettings class
        let photoSettings = AVCapturePhotoSettings()
        // Set photo settings for our need
        photoSettings.isAutoStillImageStabilizationEnabled = true
        photoSettings.isHighResolutionPhotoEnabled = true
        
        let shutterView = UIView(frame: cameraView.frame)
        shutterView.backgroundColor = UIColor.black
        view.addSubview(shutterView)
        UIView.animate(withDuration: 0.3, animations: {
            shutterView.alpha = 0
        }, completion: { (_) in
            shutterView.removeFromSuperview()
        })

        // Call capturePhoto method by passing our photo settings and a delegate implementing AVCapturePhotoCaptureDelegate
        capturePhotoOutput.capturePhoto(with: photoSettings, delegate: self)
    }
    
    @objc func closeCamera(_ sender: UIButton) {
        videoPreviewLayer?.isHidden = true
        captureSession?.stopRunning()
        cameraView.removeFromSuperview()
        viewCamera.isHidden = true
    }
    
    @objc func switchCamera(_ sender: UIButton) {
        if let session = captureSession {
            let currentCameraInput: AVCaptureInput = session.inputs[0]
            session.removeInput(currentCameraInput)
            var newCamera: AVCaptureDevice
            newCamera = AVCaptureDevice.default(for: AVMediaType.video)!
            
                if (currentCameraInput as! AVCaptureDeviceInput).device.position == .back {
                    UIView.transition(with: self.cameraView, duration: 0.4, options: .transitionFlipFromLeft, animations: {
                        newCamera = self.cameraWithPosition(.front)!
                    }, completion: nil)
                } else {
                    UIView.transition(with: self.cameraView, duration: 0.4, options: .transitionFlipFromRight, animations: {
                        newCamera = self.cameraWithPosition(.back)!
                    }, completion: nil)
                }
                do {
                    try self.captureSession?.addInput(AVCaptureDeviceInput(device: newCamera))
                }
                catch {
                    print("error: \(error.localizedDescription)")
                }
            
        }
    }
    func cameraWithPosition(_ position: AVCaptureDevice.Position) -> AVCaptureDevice? {
        let deviceDescoverySession = AVCaptureDevice.DiscoverySession.init(deviceTypes: [AVCaptureDevice.DeviceType.builtInWideAngleCamera], mediaType: AVMediaType.video, position: AVCaptureDevice.Position.unspecified)

        for device in deviceDescoverySession.devices {
            if device.position == position {
                return device
            }
        }
        return nil
    }
    // MARK:- Timer operation
    func startTimerOperation () {
        seconds = TimerOperation.remainingTime
        if self.countdownTimer == nil {
            self.countdownTimer = Timer.scheduledTimer(timeInterval: 1, target: self, selector: #selector(self.updateTimer), userInfo: nil, repeats: true)
        }
        self.countdownTimerView.start(beginingValue: TimerOperation.totalTime, interval: 1, elapsedInterval: TimeInterval(TimerOperation.totalTime - seconds))
    }
    
    @objc func updateTimer() {
        if seconds < 1 {
            self.stopTimer()
            sessionCompleted()
        } else {
            seconds -= 1
            lblTimer.text = timeString(time: TimeInterval(seconds))
        }
    }
    func timeString(time:TimeInterval) -> String {
        let hours = Int(time) / 3600
        let minutes = Int(time) / 60 % 60
        let seconds = Int(time) % 60
        return String(format:"%02i:%02i:%02i", hours, minutes, seconds)
    }
    
    func stopTimer() {
        if countdownTimer != nil {
            countdownTimer?.invalidate()
            countdownTimer = nil
        }
    }
    func getCurrentDateInUTCFormate() -> String {
        
        let currentDate             = Date()
        let dateFormatter           = DateFormatter()
        
        dateFormatter.locale        = Locale(identifier: "en_US_POSIX")
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        print("Desired date = \(dateFormatter.string(from: currentDate as Date))")
        return dateFormatter.string(from: currentDate as Date)
    }
    
    //  MARK:- API methods
    func uploadPhoto(image : UIImage) {
        //"order_slot_id" : order.slotId
        let param : HTTPParameters = ["order_id" : order.id!, "created_at" : getCurrentDateInUTCFormate()]
        
        var images = [String: UIImage]()
        images["photo"] = image
        
        APIComponents.Photographer.uploadPhoto(parameter: param, images: images) { [weak self] (success, data, error) in
            print(param)
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
          }
    }
  
    func saveImageIndirectory(image : UIImage) {
        let documentDirectoryPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let fileURL = documentDirectoryPath.appendingPathComponent("FAN/")
        do {
            try UIImageJPEGRepresentation(image, 1.0)?.write(to: fileURL)
            print("Added")
        }
        catch {
            print(error.localizedDescription)
        }
        getImage()
    }
    
    func getImage() {
        let documentDirectoryPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
        let myFilesPath = documentDirectoryPath.appending("/FAN")
        if FileManager.default.fileExists(atPath: myFilesPath) {
            DispatchQueue.global().async {
                self.uploadPhoto(image: UIImage(contentsOfFile: myFilesPath)!)
            }
        }
    }
    
    //  MARK:- Action methods
    @IBAction func btnBack_Action(_ sender: UIButton) {
        // Go back
        self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func btnCaptureImage(_ sender: UIButton) {
        
        cameraView.frame = UIScreen.main.bounds
        viewCamera.isHidden = false
        
        let previewView = UIView(frame: CGRect(x: 0, y: 0, width: Screen.width, height: Screen.height))
        cameraView.addSubview(previewView)
        
        let captureButton = UIButton(frame: CGRect(x: 0, y: Screen.height - 80, width: 50, height: 50))
        captureButton.center.x = self.view.center.x
        captureButton.layer.cornerRadius = captureButton.frame.size.width / 2
        captureButton.clipsToBounds = true
        captureButton.backgroundColor = UIColor.white
        captureButton.addTarget(self, action: #selector(StartShootVC.capture(_:)), for: .touchUpInside)
        cameraView.addSubview(captureButton)
        
        let closeButton = UIButton(frame: CGRect(x: 10, y: Device.iPhoneX ? 18 : 12, width: 50, height: 50))
        closeButton.setImage(#imageLiteral(resourceName: "cancel"), for: .normal)
        closeButton.addTarget(self, action: #selector(StartShootVC.closeCamera(_:)), for: .touchUpInside)
        cameraView.addSubview(closeButton)
        
        let cameraSwitchButton = UIButton(frame: CGRect(x: Screen.width - 56, y: Device.iPhoneX ? 18 : 12, width: 40, height: 54))
        cameraSwitchButton.setImage(#imageLiteral(resourceName: "camera-switch"), for: .normal)
        cameraSwitchButton.addTarget(self, action: #selector(StartShootVC.switchCamera(_:)), for: .touchUpInside)
        cameraView.addSubview(cameraSwitchButton)
        self.view.addSubview(cameraView)
        // Get an instance of the AVCaptureDevice class to initialize a device object and provide the video as the media type parameter
        guard let captureDevice = AVCaptureDevice.default(for: AVMediaType.video) else {
            fatalError("No video device found")
        }

        do {
            // Get an instance of the AVCaptureDeviceInput class using the previous deivce object
            let input = try AVCaptureDeviceInput(device: captureDevice)

            // Initialize the captureSession object
            captureSession = AVCaptureSession()

            // Set the input devcie on the capture session
            captureSession?.addInput(input)
        
            // Get an instance of ACCapturePhotoOutput class
            capturePhotoOutput = AVCapturePhotoOutput()
            capturePhotoOutput?.isHighResolutionCaptureEnabled = true

            // Set the output on the capture session
            captureSession?.addOutput(capturePhotoOutput!)

            // Initialize a AVCaptureMetadataOutput object and set it as the input device
            let captureMetadataOutput = AVCaptureMetadataOutput()
            captureSession?.addOutput(captureMetadataOutput)

            // Set delegate and use the default dispatch queue to execute the call back
            captureMetadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
           // captureMetadataOutput.metadataObjectTypes = [AVMetadataObject.ObjectType.qr]

            //Initialise the video preview layer and add it as a sublayer to the viewPreview view's layer
            videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession!)
            videoPreviewLayer?.videoGravity = AVLayerVideoGravity.resizeAspectFill
            videoPreviewLayer?.frame = view.layer.bounds
            previewView.layer.addSublayer(videoPreviewLayer!)

            //start video capture
            captureSession?.startRunning()

            TimerOperation.shared.saveTimeOperation(isTimerRunning: true, remainingTime: seconds)

        } catch {
            //If any error occurs, simply print it out
            print(error)
            return
        }

    }
    
    @objc func dismissCamera() {
        self.dismiss(animated: true, completion: nil)
    }
    
    @objc func captureImage() {
       // self.imagePickerController?.takePicture()
    }
}

extension StartShootVC : AVCapturePhotoCaptureDelegate {
    func photoOutput(_ captureOutput: AVCapturePhotoOutput,
                     didFinishProcessingPhoto photoSampleBuffer: CMSampleBuffer?,
                     previewPhoto previewPhotoSampleBuffer: CMSampleBuffer?,
                     resolvedSettings: AVCaptureResolvedPhotoSettings,
                     bracketSettings: AVCaptureBracketedStillImageSettings?,
                     error: Error?) {
        // Make sure we get some photo sample buffer
        guard error == nil,
            let photoSampleBuffer = photoSampleBuffer else {
                print("Error capturing photo: \(String(describing: error))")
                return
        }
        
        // Convert photo same buffer to a jpeg image data by using AVCapturePhotoOutput
        guard let imageData = AVCapturePhotoOutput.jpegPhotoDataRepresentation(forJPEGSampleBuffer: photoSampleBuffer, previewPhotoSampleBuffer: previewPhotoSampleBuffer) else {
            return
        }
        
        // Initialise an UIImage with our image data
        let capturedImage = UIImage.init(data: imageData , scale: 1.0)
        let fixedImage = capturedImage?.fixedOrientation()
        if let image = fixedImage {
            // Save our captured image to photos album
            saveImageIndirectory(image: image)
        }
    }
}

extension StartShootVC : AVCaptureMetadataOutputObjectsDelegate {
    func metadataOutput(_ captureOutput: AVCaptureMetadataOutput,
                        didOutput metadataObjects: [AVMetadataObject],
                        from connection: AVCaptureConnection) {
        }
    }

