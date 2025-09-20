# ADR 0001: Adopt Maven Multi-Module Architecture

## Status
Accepted (2025-09-19)

## Context
The application must integrate UI, media processing, AI workloads, and packaging. Keeping everything in a single module complicates dependency management, testing, and packaging (jpackage) for multiple platforms. We also need to enforce clean boundaries between UI, domain, AI, and media layers.

## Decision
Adopt a Maven multi-module project with the following modules:
- `app-core` for domain models, services, configuration, and shared utilities.
- `app-media` for JavaCV/OpenCV integration and media pipeline building blocks.
- `app-ai` for AI adapters (Vosk, ONNX Runtime) and processing nodes.
- `app-ui` for JavaFX views/controllers/resources.
- `app-launcher` as the executable assembly that depends on all other modules and acts as the jpackage target.

Each module publishes a clear API surface to dependent modules. Unit tests reside in their respective modules, while integration tests may live in `app-launcher`.

## Consequences
- Clear ownership and boundaries; easier to parallelize development.
- Enables selective testing and packaging; e.g., `app-ai` can be excluded from light builds if needed.
- Requires additional Maven configuration and shared version management but simplifies long-term maintenance.
- Increases initial setup effort; mitigated by templates and parent POM management.
