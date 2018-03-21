Accessibility Test Framework for Android
----------------------------------------
To help people with disabilities access Android apps, developers of those apps
need to consider how their apps will be presented to accessibility services.
Some good practices can be checked by automated tools, such as if a View has a
contentDescription. Other rules require human judgment, such as whether or not
a contentDescription makes sense to all users.

For more information about Mobile Accessibility, see:
https://www.w3.org/WAI/mobile/

For more information about accessibility testing for Android applications, see:
https://developer.android.com/training/accessibility/testing.html

This library collects various accessibility-related checks on View objects as
well as AccessibilityNodeInfo objects (which the Android framework derives from
Views and sends to AccessibilityServices).

Building the Library
--------------------
The supplied build.gradle file can be used to build the Accessibility Test
Framework with gradle. The build.gradle file can also be used to import the
project to Android Studio.

Sample Usage
------------
Given a view, the following code runs all accessibility checks on all views in the
hierarchy rooted at that view and throws an exception if any errors are found:

Set<AccessibilityHierarchyCheck> checks =
    AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
        AccessibilityCheckPreset.LATEST);
AccessibilityHierarchy hierarchy = AccessibilityHierarchy.newBuilder(node, context).build();
List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();
for (AccessibilityHierarchyCheck check : checks) {
  results.addAll(check.runCheckOnHierarchy(hierarchy));
}
List<AccessibilityHierarchyCheckResult> errors =
    AccessibilityCheckResultUtils.getResultsForType(
        results, AccessibilityCheckResultType.ERROR);
if (errors.size() > 0) {
  throw new RuntimeException(errors.get(0).getMessage().toString());
}
