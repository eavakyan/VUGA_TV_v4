//
//  CirculerProgress.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 14/06/24.
//

import Foundation
import UIKit
import SwiftUI

struct PieProgress: View {
     var progress: Float

    var body: some View {
        Circle()
            .stroke(Color.text, lineWidth: 1)
            .overlay(
                PieShape(progress: Double(self.progress))
                    .foregroundColor(.text)
            )
            .frame(maxWidth: .infinity)
            .animation(Animation.linear, value: progress)
            .aspectRatio(contentMode: .fit)
    }
}

struct PieShape: Shape {
    var progress: Double = 0.0

    var animatableData: Double {
        get {
            self.progress
        }
        set {
            self.progress = newValue
        }
    }

    private let startAngle: Double = (Double.pi) * 1.5
    private var endAngle: Double {
        get {
            return self.startAngle + Double.pi * 2 * self.progress
        }
    }

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let arcCenter =  CGPoint(x: rect.size.width / 2, y: rect.size.width / 2)
        let radius = rect.size.width / 2
        path.move(to: arcCenter)
        path.addArc(center: arcCenter, radius: radius, startAngle: Angle(radians: startAngle), endAngle: Angle(radians: endAngle), clockwise: false)
        path.closeSubpath()
        return path
    }
}

struct CircularProgressView: View {
    let progress: Double
    
    var body: some View {
        ZStack {
            Circle()
                .stroke(
                    Color.base.opacity(0.3),
                    lineWidth: 5
                )
            Circle()
                .trim(from: 0, to: progress)
                .stroke(
                    Color.base,
                    style: StrokeStyle(
                        lineWidth: 5,
                        lineCap: .round
                    )
                )
                .rotationEffect(.degrees(-90))
                .animation(.easeOut, value: progress)
        }
    }
}
