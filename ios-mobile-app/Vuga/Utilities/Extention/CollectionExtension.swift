//
//  CollectionExtension.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 18/05/24.
//

import Foundation

extension Collection where Indices.Iterator.Element == Index {
   public subscript(safe index: Index) -> Iterator.Element? {
     return (startIndex <= index && index < endIndex) ? self[index] : nil
   }
}

extension Collection {
    var isNotEmpty : Bool {
        !isEmpty
    }
}

