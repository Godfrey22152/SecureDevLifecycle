# Android Screen Time & Blocking Integration Guide

On Android, there isn't a single "Screen Time API" as on iOS. Instead, you combine several services to achieve similar functionality.

## 1. Monitoring Usage: UsageStatsManager

The `UsageStatsManager` provides access to device usage history and statistics.

### Permission required in AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />
```

### Checking Usage:
```kotlin
val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
val endTime = System.currentTimeMillis()
val startTime = endTime - 1000 * 60 * 60 // 1 hour ago

val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
```

## 2. Blocking Apps: AccessibilityService

An `AccessibilityService` can detect when an app is launched by listening to `TYPE_WINDOW_STATE_CHANGED` events.

### Implementation:
```kotlin
class ScreenBlockerService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (isAppBlocked(packageName)) {
                // Redirect user to a "Blocked" screen
                val intent = Intent(this, BlockedActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    private fun isAppBlocked(packageName: String?): Boolean {
        // Logic to check against your blocked list (synced from backend)
        return false
    }

    override fun onInterrupt() {}
}
```

## 3. Alternative: DevicePolicyManager

For more robust blocking (e.g., in a parental control or enterprise app), you can use the `DevicePolicyManager` to set application restrictions or hide apps.

```kotlin
val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

if (dpm.isAdminActive(adminComponent)) {
    dpm.setApplicationHidden(adminComponent, "com.blocked.app", true)
}
```

## 4. Syncing with Backend

Similar to iOS, ensure you sync the blocked list and usage stats with your Spring Boot backend.

```kotlin
fun syncBlockedApps() {
    // Call GET /api/blocked-apps/{userId}
}
```

## 5. Security & Privacy Considerations

- **Accessibility Service Security**: Accessibility services can access highly sensitive data. Ensure your service only listens for the minimum events needed (`TYPE_WINDOW_STATE_CHANGED`).
- **Permission Requests**: Clearly explain to the user *why* you need Accessibility and Usage Stats permissions. Google Play has strict policies regarding the use of these APIs.
- **Secure Communication**: Use TLS/HTTPS for all backend communication. Implement Certificate Pinning if necessary for higher security.
- **Data Minimization**: Store as little usage data as possible on your servers to minimize the impact of a potential data breach.
