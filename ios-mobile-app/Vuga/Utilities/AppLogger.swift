//
//  AppLogger.swift
//  Vuga
//
//  Logging system that works in both Debug and Release builds
//

import Foundation
import os.log

final class AppLogger {
    static let shared = AppLogger()
    private let logger = Logger(subsystem: "com.vugaenterprises.vuga", category: "app")
    
    private init() {}
    
    // Log levels
    enum LogLevel: String {
        case debug = "🔍 DEBUG"
        case info = "ℹ️ INFO"
        case warning = "⚠️ WARNING"
        case error = "❌ ERROR"
        case network = "🌐 NETWORK"
        case success = "✅ SUCCESS"
    }
    
    func log(_ message: String, level: LogLevel = .info, file: String = #file, function: String = #function, line: Int = #line) {
        let filename = URL(fileURLWithPath: file).lastPathComponent
        let logMessage = "[\(level.rawValue)] \(filename):\(line) - \(function): \(message)"
        
        // Always log to system console (visible in Console.app on Mac when device is connected)
        switch level {
        case .debug:
            logger.debug("\(logMessage)")
        case .info:
            logger.info("\(logMessage)")
        case .warning:
            logger.warning("\(logMessage)")
        case .error:
            logger.error("\(logMessage)")
        case .network:
            logger.info("\(logMessage)")
        case .success:
            logger.info("\(logMessage)")
        }
        
        // Also save to UserDefaults for in-app viewing
        saveToUserDefaults(logMessage)
    }
    
    private func saveToUserDefaults(_ message: String) {
        var logs = UserDefaults.standard.stringArray(forKey: "app_logs") ?? []
        let timestamp = DateFormatter.localizedString(from: Date(), dateStyle: .none, timeStyle: .medium)
        logs.append("\(timestamp): \(message)")
        
        // Keep only last 200 logs
        if logs.count > 200 {
            logs = Array(logs.suffix(200))
        }
        
        UserDefaults.standard.set(logs, forKey: "app_logs")
    }
    
    // Get logs for viewing in app (useful for TestFlight debugging)
    static func getRecentLogs() -> String {
        let logs = UserDefaults.standard.stringArray(forKey: "app_logs") ?? []
        return logs.joined(separator: "\n")
    }
    
    static func clearLogs() {
        UserDefaults.standard.removeObject(forKey: "app_logs")
    }
}

// Convenience global functions
func logDebug(_ message: String, file: String = #file, function: String = #function, line: Int = #line) {
    AppLogger.shared.log(message, level: .debug, file: file, function: function, line: line)
}

func logInfo(_ message: String, file: String = #file, function: String = #function, line: Int = #line) {
    AppLogger.shared.log(message, level: .info, file: file, function: function, line: line)
}

func logWarning(_ message: String, file: String = #file, function: String = #function, line: Int = #line) {
    AppLogger.shared.log(message, level: .warning, file: file, function: function, line: line)
}

func logError(_ message: String, file: String = #file, function: String = #function, line: Int = #line) {
    AppLogger.shared.log(message, level: .error, file: file, function: function, line: line)
}

func logNetwork(_ message: String, file: String = #file, function: String = #function, line: Int = #line) {
    AppLogger.shared.log(message, level: .network, file: file, function: function, line: line)
}

func logSuccess(_ message: String, file: String = #file, function: String = #function, line: Int = #line) {
    AppLogger.shared.log(message, level: .success, file: file, function: function, line: line)
}