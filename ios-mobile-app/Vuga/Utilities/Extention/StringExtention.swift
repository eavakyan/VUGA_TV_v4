//
//  StringExtention.swift
//  Vuga
//
//

import Foundation
import UIKit


func stringFromTimeInterval(interval: TimeInterval,timer: Bool = false) -> String {
    
    if interval.isNaN {
        return ""
    }
    
    let ti = NSInteger(interval)
    
    let seconds = ti % 60
    let minutes = (ti / 60) % 60
    let hours = (ti / 3600)
    
    if !timer {
        if hours > 0 {
            return String(format: "%0.2d:%0.2d:%0.2d",hours,minutes,seconds)
        } else {
            return String(format: "%0.2d:%0.2d",minutes,seconds)
        }
    } else {
        if hours > 0 {
            return String("\(hours)h \(minutes)m \(seconds)s")
        } else {
            return String("\(minutes)m \(seconds)s")
        }
    }
}

extension Double {
  func asString(style: DateComponentsFormatter.UnitsStyle) -> String {
    let formatter = DateComponentsFormatter()
    formatter.allowedUnits = [.hour, .minute, .second, .nanosecond]
    formatter.unitsStyle = style
    return formatter.string(from: self) ?? ""
  }
}


import UIKit

import UIKit

extension NSAttributedString {
    internal convenience init?(html: String, fontSize: CGFloat) {
        // Convert the HTML string to data using UTF-8 encoding
        guard let data = html.data(using: String.Encoding.utf8, allowLossyConversion: false) else {
            return nil
        }
        
        // Print the HTML string for debugging purposes
        print(html)
        
        // Try to create an attributed string from the HTML data
        guard let attributedString = try? NSAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding: String.Encoding.utf8.rawValue], documentAttributes: nil) else {
            return nil
        }
        
        // Create a mutable copy to apply attributes
        let mutableAttributedString = NSMutableAttributedString(attributedString: attributedString)
        
        // Increase the font size attribute for the entire string
        mutableAttributedString.enumerateAttributes(in: NSRange(location: 0, length: mutableAttributedString.length), options: []) { (attributes, range, _) in
            let currentFont = attributes[.font] as? UIFont ?? UIFont.systemFont(ofSize: UIFont.systemFontSize)
            let newFont = currentFont.withSize(fontSize)
            mutableAttributedString.addAttribute(.font, value: newFont, range: range)
        }
        
        // Apply white color attribute to the entire string
        let whiteColorAttribute: [NSAttributedString.Key: Any] = [.foregroundColor: UIColor.white]
        mutableAttributedString.addAttributes(whiteColorAttribute, range: NSRange(location: 0, length: mutableAttributedString.length))
        
        // Initialize self with the modified attributed string
        self.init(attributedString: mutableAttributedString)
    }
}


extension String {
    /// Converts a date string from "dd-MM-yyyy" format to "dd MMMM, yyyy" format.
    func toFormattedDateString() -> String? {
        let inputDateFormatter = DateFormatter()
        inputDateFormatter.dateFormat = "dd-MM-yyyy"
        
        // Convert the string to a Date object
        if let date = inputDateFormatter.date(from: self) {
            let outputDateFormatter = DateFormatter()
            outputDateFormatter.dateFormat = "dd MMMM, yyyy"
            return outputDateFormatter.string(from: date)
        } else {
            return nil
        }
    }
    
    /// Formats duration string from minutes to human-readable format with units
    func formatDurationWithUnits() -> String {
        // Try to convert string to integer minutes
        guard let totalMinutes = Int(self) else {
            return self // Return as-is if not a valid number
        }
        
        if totalMinutes < 60 {
            // Less than an hour - show only minutes
            return "\(totalMinutes) Min"
        } else {
            // An hour or more - show hours and minutes
            let hours = totalMinutes / 60
            let minutes = totalMinutes % 60
            
            if minutes == 0 {
                // Exactly X hours
                return "\(hours) H"
            } else {
                // X hours and Y minutes
                return "\(hours) H \(minutes) Min"
            }
        }
    }
}
