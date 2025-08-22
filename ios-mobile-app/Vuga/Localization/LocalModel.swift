//
//  LocalModel.swift
//  Translate it
//
//

import Foundation

struct Lang {
    var localName : String
    var nameInEnglish: String
    var language : Language
}

let appLanguages = [
    Lang(localName: "العربية", nameInEnglish: "Arabic" ,language: .Arabic),
    Lang(localName: "中文", nameInEnglish: "Chinese" ,language: .Chinese),
    Lang(localName: "English", nameInEnglish: "English" ,language: .English),
    Lang(localName: "Dansk", nameInEnglish: "Danish" ,language: .Danish),
    Lang(localName: "Nederlands", nameInEnglish: "Dutch" ,language: .Dutch),
    Lang(localName: "Français", nameInEnglish: "French" ,language: .French),
    Lang(localName: "Deutsch", nameInEnglish: "German" ,language: .German),
    Lang(localName: "Ελληνικά", nameInEnglish: "Greek" ,language: .Greek),
    Lang(localName: "हिन्दी", nameInEnglish: "Hindi" ,language: .Hindi),
    Lang(localName: "Bahasa Indonesia", nameInEnglish: "Indonesian" ,language: .Indonesian),
    Lang(localName: "Italiano", nameInEnglish: "Italian" ,language: .Italian),
    Lang(localName: "日本語", nameInEnglish: "Japanese" ,language: .Japanese),
    Lang(localName: "한국어", nameInEnglish: "Korean" ,language: .Korean),
    Lang(localName: "Norsk", nameInEnglish: "Norwegian" ,language: .Norwegian),
    Lang(localName: "Polski", nameInEnglish: "Polish" ,language: .Polish),
    Lang(localName: "Português", nameInEnglish: "Portuguese" ,language: .Portuguese),
    Lang(localName: "Русский", nameInEnglish: "Russian" ,language: .Russian),
    Lang(localName: "Español", nameInEnglish: "Spanish" ,language: .Spanish),
    Lang(localName: "Svenska", nameInEnglish: "Swedish" ,language: .Swedish),
    Lang(localName: "ไทย", nameInEnglish: "Thai" ,language: .Thai),
    Lang(localName: "Türkçe", nameInEnglish: "Turkish" ,language: .Turkish),
    Lang(localName: "Tiếng Việt", nameInEnglish: "Vietnamese" ,language: .Vietnamese),
]


enum Language: String {
    case Arabic = "ar"
    case Chinese = "zh-Hans"
    case English = "en"
    case Danish = "da"
    case Dutch = "nl"
    case French = "fr"
    case German = "de"
    case Greek = "el"
    case Hindi = "hi"
    case Indonesian = "id"
    case Italian = "it"
    case Japanese = "ja"
    case Korean = "ko"
    case Norwegian = "no"
    case Polish = "pl"
    case Portuguese = "pt"
    case Russian = "ru"
    case Spanish = "es"
    case Swedish = "sv"
    case Thai = "th"
    case Turkish = "tr"
    case Vietnamese = "vi"
    
}

extension String {

    /// Localizes a string using given language from Language enum.
    /// - parameter language: The language that will be used to localized string.
    /// - Returns: localized string.
    func localized(_ language: Language = LocalizationService.shared.language) -> String {
        let path = Bundle.main.path(forResource: language.rawValue, ofType: "lproj")
        let bundle: Bundle
        if let path = path {
            bundle = Bundle(path: path) ?? .main
        } else {
            bundle = .main
        }
        return localized(bundle: bundle)
    }

    /// Localizes a string using given language from Language enum.
    ///  - Parameters:
    ///  - language: The language that will be used to localized string.
    ///  - args:  dynamic arguments provided for the localized string.
    /// - Returns: localized string.
    func localized(_ language: Language, args arguments: CVarArg...) -> String {
        let path = Bundle.main.path(forResource: language.rawValue, ofType: "lproj")
        let bundle: Bundle
        if let path = path {
            bundle = Bundle(path: path) ?? .main
        } else {
            bundle = .main
        }
        return String(format: localized(bundle: bundle), arguments: arguments)
    }

    /// Localizes a string using self as key.
    ///
    /// - Parameters:
    ///   - bundle: the bundle where the Localizable.strings file lies.
    /// - Returns: localized string.
    private func localized(bundle: Bundle) -> String {
        return NSLocalizedString(self, tableName: nil, bundle: bundle, value: "", comment: "")
    }
}

class LocalizationService {

    static let shared = LocalizationService()
    static let changedLanguage = Notification.Name("changedLanguage")

    private init() {}
    
    var language: Language {
        get {
            guard let languageString = UserDefaults.standard.string(forKey: "language") else {
                
                
                return Language(rawValue: String(Locale.preferredLanguages.first?.prefix(2) ?? "")) ?? .English
            }
            return Language(rawValue: languageString) ?? (Language(rawValue: String(Locale.preferredLanguages.first?.prefix(2) ?? "")) ?? .English)
        } set {
            if newValue != language {
                UserDefaults.standard.setValue(newValue.rawValue, forKey: "language")
                NotificationCenter.default.post(name: LocalizationService.changedLanguage, object: nil)
            }
        }
    }
}

