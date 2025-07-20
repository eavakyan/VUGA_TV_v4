//
//  DataController.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 10/06/24.
//

import Foundation
import CoreData

class DataController: ObservableObject {
    let container : NSPersistentContainer
    static var shared = DataController()
    
    init() {
        let iCloud = false
        let name = "Flixy"
            container = NSPersistentContainer(name: name)
        guard let description = container.persistentStoreDescriptions.first else { fatalError("No description found") }
        
            description.setOption(true as NSNumber, forKey: NSPersistentHistoryTrackingKey)
        
        description.setOption(true as NSNumber, forKey: NSPersistentStoreRemoteChangeNotificationPostOptionKey)
        
        container.loadPersistentStores { (storeDescription, error) in
            if let error = error as NSError? { fatalError("Unresolved error \(error), \(error.userInfo)") }
        }
    }
    
    var context : NSManagedObjectContext  {
        DataController.shared.container.viewContext
    }
    
    func saveData() {
        try? DataController.shared.container.viewContext.save()
    }
    
}
