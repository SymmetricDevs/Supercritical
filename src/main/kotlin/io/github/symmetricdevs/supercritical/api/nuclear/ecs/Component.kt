package io.github.symmetricdevs.supercritical.api.nuclear.ecs

/**
 * Marker interface for ECS components.
 *
 * Implementations should be plain data classes. Systems may read or write them,
 * but components must not contain behavior that reaches outside the simulation.
 */
interface Component
