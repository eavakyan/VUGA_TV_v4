//
//  QRScannerView.swift
//  Vuga
//
//

import SwiftUI
import AVFoundation

struct QRScannerView: View {
    @StateObject private var viewModel = QRScannerViewModel()
    @State private var showingAlert = false
    @State private var alertMessage = ""
    @State private var isProcessing = false
    @Environment(\.presentationMode) var presentationMode
    
    // Optional session token if coming from deep link
    let sessionToken: String?
    
    init(sessionToken: String? = nil) {
        self.sessionToken = sessionToken
    }
    
    var body: some View {
        ZStack {
            if viewModel.isCameraAuthorized {
                CameraPreview(session: viewModel.session)
                    .ignoresSafeArea()
                
                VStack {
                    HStack {
                        Button(action: {
                            presentationMode.wrappedValue.dismiss()
                        }) {
                            Image(systemName: "xmark.circle.fill")
                                .font(.system(size: 30))
                                .foregroundColor(.white)
                                .background(Color.black.opacity(0.5))
                                .clipShape(Circle())
                        }
                        .padding()
                        
                        Spacer()
                    }
                    
                    Spacer()
                    
                    VStack(spacing: 20) {
                        Image(systemName: "qrcode.viewfinder")
                            .font(.system(size: 200))
                            .foregroundColor(.white)
                            .opacity(0.8)
                        
                        Text("Scan TV QR Code")
                            .font(.title2)
                            .fontWeight(.semibold)
                            .foregroundColor(.white)
                        
                        Text("Position the QR code within the frame")
                            .font(.body)
                            .foregroundColor(.white.opacity(0.8))
                    }
                    .padding()
                    
                    Spacer()
                    
                    if isProcessing {
                        HStack {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            Text("Authenticating...")
                                .foregroundColor(.white)
                                .padding(.leading, 10)
                        }
                        .padding()
                        .background(Color.black.opacity(0.7))
                        .cornerRadius(10)
                        .padding(.bottom, 50)
                    }
                }
            } else {
                VStack(spacing: 20) {
                    Image(systemName: "camera.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    
                    Text("Camera Access Required")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    Text("Please enable camera access in Settings to scan QR codes")
                        .multilineTextAlignment(.center)
                        .foregroundColor(.gray)
                        .padding(.horizontal)
                    
                    Button("Open Settings") {
                        if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(settingsUrl)
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    
                    Button("Cancel") {
                        presentationMode.wrappedValue.dismiss()
                    }
                    .buttonStyle(.bordered)
                }
                .padding()
            }
        }
        .addBackground()
        .alert(isPresented: $showingAlert) {
            Alert(
                title: Text("Authentication"),
                message: Text(alertMessage),
                dismissButton: .default(Text("OK")) {
                    if alertMessage.contains("Success") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            )
        }
        .onAppear {
            // If we have a session token from deep link, authenticate immediately
            if let sessionToken = sessionToken {
                isProcessing = true
                authenticateTVSession(sessionToken: sessionToken)
            } else {
                // Otherwise, start QR scanning
                viewModel.checkCameraPermission()
                viewModel.onQRCodeScanned = { code in
                    handleQRCode(code)
                }
            }
        }
        .onDisappear {
            viewModel.stopSession()
        }
    }
    
    private func handleQRCode(_ code: String) {
        guard !isProcessing else { return }
        
        // Parse the QR code - expecting format: vuga://auth/tv/{session_token}
        if let url = URL(string: code),
           url.scheme == "vuga",
           url.host == "auth",
           url.pathComponents.count >= 3,
           url.pathComponents[1] == "tv" {
            
            let sessionToken = url.pathComponents[2]
            isProcessing = true
            
            // Authenticate with the TV
            authenticateTVSession(sessionToken: sessionToken)
        } else {
            alertMessage = "Invalid QR code. Please scan a VUGA TV authentication code."
            showingAlert = true
        }
    }
    
    private func authenticateTVSession(sessionToken: String) {
        print("QRScanner: Starting authentication with session token: \(sessionToken)")
        
        let profileViewModel = ProfileViewModel()
        
        guard profileViewModel.myUser != nil else {
            print("QRScanner: User not logged in")
            isProcessing = false
            alertMessage = "Please log in to your account first"
            showingAlert = true
            return
        }
        
        print("QRScanner: User is logged in, calling API...")
        
        APIClient.shared.authenticateTVSession(sessionToken: sessionToken) { success, message in
            print("QRScanner: API response received - success: \(success), message: \(message ?? "nil")")
            
            DispatchQueue.main.async {
                isProcessing = false
                if success {
                    alertMessage = "Success! Your TV has been authenticated."
                    showingAlert = true
                    // Dismiss the view after a short delay
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                        self.presentationMode.wrappedValue.dismiss()
                    }
                } else {
                    alertMessage = message ?? "Failed to authenticate TV. Please try again."
                    showingAlert = true
                }
            }
        }
    }
}

class QRScannerViewModel: NSObject, ObservableObject {
    @Published var isCameraAuthorized = false
    var session = AVCaptureSession()
    private let metadataOutput = AVCaptureMetadataOutput()
    private let sessionQueue = DispatchQueue(label: "qr-scanner-session")
    var onQRCodeScanned: ((String) -> Void)?
    
    func checkCameraPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            isCameraAuthorized = true
            setupCamera()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    self?.isCameraAuthorized = granted
                    if granted {
                        self?.setupCamera()
                    }
                }
            }
        default:
            isCameraAuthorized = false
        }
    }
    
    private func setupCamera() {
        sessionQueue.async { [weak self] in
            guard let self = self else { return }
            
            self.session.beginConfiguration()
            
            guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else { return }
            
            do {
                let videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
                
                if self.session.canAddInput(videoInput) {
                    self.session.addInput(videoInput)
                }
                
                if self.session.canAddOutput(self.metadataOutput) {
                    self.session.addOutput(self.metadataOutput)
                    
                    self.metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
                    self.metadataOutput.metadataObjectTypes = [.qr]
                }
            } catch {
                print("Error setting up camera: \(error)")
            }
            
            self.session.commitConfiguration()
            self.session.startRunning()
        }
    }
    
    func stopSession() {
        sessionQueue.async { [weak self] in
            self?.session.stopRunning()
        }
    }
}

extension QRScannerViewModel: AVCaptureMetadataOutputObjectsDelegate {
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        guard let metadataObject = metadataObjects.first,
              let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject,
              let stringValue = readableObject.stringValue else { return }
        
        // Vibrate on successful scan
        AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
        
        // Stop scanning to prevent multiple scans
        session.stopRunning()
        
        onQRCodeScanned?(stringValue)
    }
}

struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession
    
    func makeUIView(context: Context) -> VideoPreviewView {
        let view = VideoPreviewView()
        view.backgroundColor = .black
        view.videoPreviewLayer.session = session
        view.videoPreviewLayer.videoGravity = .resizeAspectFill
        view.videoPreviewLayer.connection?.videoOrientation = .portrait
        return view
    }
    
    func updateUIView(_ uiView: VideoPreviewView, context: Context) {
        // Update if needed
    }
    
    class VideoPreviewView: UIView {
        override class var layerClass: AnyClass {
            AVCaptureVideoPreviewLayer.self
        }
        
        var videoPreviewLayer: AVCaptureVideoPreviewLayer {
            return layer as! AVCaptureVideoPreviewLayer
        }
    }
}