#!/bin/bash

# Fix all dismiss environment usage
FILES=(
    "Vuga/Views/AgeRatingBadgeView.swift"
    "Vuga/Views/Checkout/CheckoutView.swift"
    "Vuga/Views/ExpandableDescriptionView.swift"
    "Vuga/Views/ProfileSelectionView.swift"
    "Vuga/Views/SubtitleTrackSelectionView.swift"
)

for file in "${FILES[@]}"; do
    echo "Fixing $file..."
    # Replace @Environment(\.dismiss) with presentationMode
    sed -i '' 's/@Environment(\\.dismiss) var dismiss/@Environment(\\.presentationMode) var presentationMode: Binding<PresentationMode>/g' "$file"
    # Replace dismiss() calls
    sed -i '' 's/dismiss()/presentationMode.wrappedValue.dismiss()/g' "$file"
done

echo "All files fixed!"