//
//  IntExtension.swift
//  Vuga
//
//

import Foundation

extension Int {
    var roundedWithAbbreviations: String {
        let number = Double(self)
        let thousand = number / 1000
        let million = number / 1000000
        let billion = number / 1000000000
        if billion >= 1.0 {
            return "\(String(format: "%.g", round(billion*10)/10))b"
        }
        if million >= 1.0 {
            return "\(String(format: "%.g", round(million*10)/10))m"
        }
        else if thousand >= 1.0 {
            return "\(String(format: "%.1f", round(thousand*10)/10))k"
        }
        else {
            return "\(self)"
        }
    }
    
    
    
    func secondsToTime() -> String {
        
        let (h,m,s) = (self / 3600, (self % 3600) / 60, (self % 3600) % 60)
        
        let h_string = h < 10 ? "0\(h)" : "\(h)"
        let m_string =  m < 10 ? "0\(m)" : "\(m)"
        let s_string =  s < 10 ? "0\(s)" : "\(s)"
        
        if self < 3599 {
            return "\(m_string):\(s_string)"
        } else {
            return "\(h_string):\(m_string):\(s_string)"
        }
    }
    
}
