//
//  EditProfileVIew.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 30/05/24.
//

import SwiftUI
import Kingfisher

struct EditProfileVIew: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm = EditProfileViewModel()
    @AppStorage(SessionKeys.myUser) var myUser : User? = nil

    var body: some View {
        VStack(spacing: 30) {
            BackBarView(title: .editProfile.localized(language))
            ZStack(alignment: .bottomTrailing) {
                if vm.image != nil {
                    Image(uiImage: vm.image!)
                        .resizeFillTo(size: 110)
                        .clipShape(.circle)
                } else if myUser?.profileImage != nil && myUser?.profileImage != "" {
                    KFImage(vm.myUser?.profileImage?.addBaseURL())
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 110,height: 110)
                        .clipShape(.circle)
                        .addStroke(radius: 100)

                } else {
                    Image.person
                        .resizeFitTo(size: 110,renderingMode: .template)
                        .foregroundColor(.text)
                }
                Image.camera
                    .resizeFitTo(size: 15)
                    .padding(7)
                    .background(Color.bg)
                    .clipShape(.circle)
                    .padding(1)
                    .background(Color.textLight)
                    .clipShape(.circle)
                    .onTap {
                        Function.shared.haptic()
                        vm.isShowImagePicker = true
                    }
            }
            VStack(spacing: 15) {
                MyTextField(placeholder: .fullName, text: $vm.fullName)
                Spacer()
                CommonButton(title: .update, onTap: {
                    vm.editProfile()
                })
                .padding(.bottom)
            }
            .padding(.top,10)
            .padding()
        }
        .sheet(isPresented: $vm.isShowImagePicker, content: {
            ImagePicker(image: $vm.image)
                .ignoresSafeArea()
        })
        .hideNavigationbar()
        .loaderView(vm.isLoading)
        .onAppear(perform: {
            if myUser != nil {
                vm.fullName = myUser?.fullname ?? ""
                vm.email = myUser?.email ?? ""
            }
        })
    }
}

#Preview {
    EditProfileVIew()
}
