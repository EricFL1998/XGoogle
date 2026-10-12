package io.github.zopulus.coogle.migration
import io.github.zopulus.coogle.FeatureConfig
import io.github.zopulus.coogle.FeaturePolicy

/** Feature switches shared by the standalone Coogle settings and migrated hooks. */
internal object LspConfig {
    fun isAssistantNativeCircleEnabledXposed() = FeatureConfig.enabled(FeaturePolicy.NAVIGATION)
    fun isOkGoogleHotwordCompatibilityEnabledXposed() = FeatureConfig.enabled(FeaturePolicy.HEY_GOOGLE)
    fun isGmsRegionRestrictionBypassEnabledXposed() = FeatureConfig.enabled(FeaturePolicy.GMS)
}
