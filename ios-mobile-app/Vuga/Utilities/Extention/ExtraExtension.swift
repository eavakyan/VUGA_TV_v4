//
//  ExtraExtension.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import Foundation

extension Optional: RawRepresentable where Wrapped: Codable {
    public var rawValue: String {
        guard let data = try? JSONEncoder().encode(self) else {
            return "{}"
        }
        return String(decoding: data, as: UTF8.self)
    }

    public init?(rawValue: String) {
        guard let value = try? JSONDecoder().decode(Self.self, from: Data(rawValue.utf8)) else {
            return nil
        }
        self = value
    }
}

extension Array where Element: Hashable {
  func unique() -> [Element] {
    var dict = [Element: Bool]()
    self.forEach {
      dict[$0] = true
    }
    return Array(dict.keys)
  }
}
