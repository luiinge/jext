package jext;

@Extension(
    provider = "test",
    name = "overrider",
    version = "1.0",
    overrides = "jext.OverridableExtension",
    extensionPoint = "jext.MyExtensionPoint"
)
public class OverriderExtension extends OverridableExtension {
}
