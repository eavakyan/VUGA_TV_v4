//
//  ArrayExtention.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 11/07/24.
//

import Foundation

extension Array {
    func unique(selector:(Element,Element)->Bool) -> Array<Element> {
        return reduce(Array<Element>()){
            if let last = $0.last {
                return selector(last,$1) ? $0 : $0 + [$1]
            } else {
                return [$1]
            }
        }
    }
}

extension Array {
    func removingDuplicates<T: Hashable>(by key: (Element) -> T) -> [Element] {
        var seen = Set<T>()
        return self.filter { seen.insert(key($0)).inserted }
    }
}
