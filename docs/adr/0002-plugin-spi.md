# ADR 0002: Define Plugin SPI via ServiceLoader

## Status
Proposed (2025-09-19)

## Context
Requirements call for a plugin system that allows third parties to add effects and export formats while keeping the core editor offline-first and maintainable. Java provides a lightweight built-in discovery mechanism (`ServiceLoader`) that works well within a modular Maven setup and plays nicely with jpackage.

## Decision
Expose plugin extension points in `app-core` under `app.plugins.api` and discover implementations with `ServiceLoader` at runtime. `PluginManager` loads providers using dedicated classloaders sourced from a `plugins/` directory next to the application config directory. Plugins declare capabilities via metadata objects so the UI can render controls conditionally.

## Consequences
- Low overhead implementation compatible with Java 17 and jpackage.
- Plugins can be added without modifying core binaries.
- Requires clear documentation and validation to avoid classpath conflicts; malicious plugins can still execute local code, so distribution will rely on trust/manual installation for now.
- Future roadmap can evolve toward sandboxing (e.g., SecurityManager alternatives, Wasm) if stricter isolation is needed.
