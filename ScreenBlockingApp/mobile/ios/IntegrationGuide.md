# iOS Screen Time API Integration Guide

Apple provides several frameworks to implement screen-blocking and usage-monitoring features securely and privately.

## 1. Core Frameworks

- **FamilyControls**: Used to request authorization and get access to the Screen Time data.
- **ManagedSettings**: Used to actually apply restrictions (e.g., blocking apps, shielding).
- **DeviceActivity**: Used to monitor device activity and trigger actions based on usage or time schedules.

## 2. Authorization

Before using any of these APIs, you must request authorization from the user.

```swift
import FamilyControls

let center = AuthorizationCenter.shared

func requestAuthorization() async {
    try? await center.requestAuthorization(for: .individual)
}
```

## 3. Selecting Apps to Block

You can use the `FamilyActivityPicker` to let users choose which apps they want to block.

```swift
import SwiftUI
import FamilyControls

struct AppPickerView: View {
    @State var selection = FamilyActivitySelection()

    var body: some View {
        FamilyActivityPicker(selection: $selection)
    }
}
```

## 4. Applying Shields (Blocking)

To block apps, you use the `ManagedSettings` framework. You can define a `DeviceActivityShield` or use `ManagedSettingsStore`.

```swift
import ManagedSettings

let store = ManagedSettingsStore()

func blockApps(selection: FamilyActivitySelection) {
    let applications = selection.applicationTokens
    let categories = selection.categoryTokens

    store.shield.applications = applications.isEmpty ? nil : applications
    store.shield.applicationCategories = categories.isEmpty ? nil : .specific(categories)
}
```

## 5. Monitoring Activity

Use `DeviceActivityCenter` to start monitoring schedules.

```swift
import DeviceActivity

let schedule = DeviceActivitySchedule(
    intervalStart: DateComponents(hour: 9, minute: 0),
    intervalEnd: DateComponents(hour: 17, minute: 0),
    repeats: true
)

let center = DeviceActivityCenter()
try? center.startMonitoring(.dailyWorkHours, during: schedule)
```

## 6. Communicating with your Backend

When settings change on the device, you should sync them with your Spring Boot backend.

```swift
func syncSettingsWithBackend(userId: String, appUsage: [String: Int]) {
    // Implement API call to your backend
}
```

## 7. Security & Privacy Considerations

- **Data Encryption**: Screen time data is sensitive. Always use HTTPS for communication and encrypt data at rest on the device using Keychain or Encrypted Core Data.
- **Entitlements**: Your app must have the `Family Controls` entitlement granted by Apple to use these APIs in production.
- **Privacy**: Only collect the minimum necessary data. Apple's API is designed to be privacy-preserving; you receive tokens rather than raw app names in many cases.
