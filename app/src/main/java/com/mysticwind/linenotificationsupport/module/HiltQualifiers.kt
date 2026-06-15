package com.mysticwind.linenotificationsupport.module

import javax.inject.Qualifier

/**
 * Container for Hilt qualifier annotations.
 * Nested annotations are accessible from Java as HiltQualifiers.MaxNotificationsPerApp, etc.
 */
class HiltQualifiers private constructor() {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MaxNotificationsPerApp

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class PackageName
}
