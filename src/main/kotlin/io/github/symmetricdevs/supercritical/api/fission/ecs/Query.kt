package io.github.symmetricdevs.supercritical.api.fission.ecs

import io.github.symmetricdevs.supercritical.api.fission.ecs.registration.ComponentTypeRegistry

/**
 * Geary-style archetype queries for the ECS [World].
 *
 * A query resolves its component types from reified generic parameters and runs a
 * trailing lambda for every entity that carries all of them — no runtime
 * [ComponentType] tokens and no intermediate query object:
 *
 * ```
 * // one component
 * world.query<FuelRodComponent> { entity, rod -> ... }
 *
 * // two components
 * world.query<FuelRodComponent, PositionComponent> { entity, rod, pos -> ... }
 *
 * // three components
 * world.query<FuelRodComponent, PositionComponent, ThermalPropertiesComponent> { entity, rod, pos, therm -> ... }
 *
 * // four components
 * world.query<FuelRodComponent, PositionComponent, ThermalPropertiesComponent, CoolantChannelComponent> { entity, rod, pos, therm, coolant -> ... }
 * ```
 *
 * This mirrors Geary's `query<A, B>().forEach { (a, b) -> ... }` shape: reified
 * type parameters with iteration baked into the call. Kotlin has no variadic
 * generics, so the arity is encoded in one `inline reified` function per arity.
 * There are no per-arity query classes.
 *
 * Registration assumption: each registered [Component] class maps to exactly one
 * [ComponentType]. The built-in reactor family registers every type through
 * [io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorComponentTypes.registerAll]
 * (invoked from [io.github.symmetricdevs.supercritical.ScritAddon.initializeAddon])
 * before any [World] ticks. [ComponentTypeRegistry] is a single global registry
 * shared by all worlds and families, which is acceptable for the current
 * single-family PWR design.
 *
 * Trade-off vs. a cached query object: each call recomputes the matching
 * archetypes by scanning the archetype lattice instead of caching them across
 * ticks. That is deliberate — the reactor runs roughly 14 entities with one
 * query per system per tick, so rescanning the tiny lattice is cheaper than
 * holding a query and tracking archetype invalidation.
 *
 * @see ComponentTypeRegistry.componentType
 */

/**
 * Iterates every entity that has component [A], invoking [action] with the entity
 * and its component. Entities whose [A] storage slot is null are skipped.
 *
 * ```
 * world.query<FuelRodComponent> { entity, rod -> ... }
 * ```
 */
inline fun <reified A : Component> World.query(noinline action: (Entity, A) -> Unit) {
    val typeA = ComponentTypeRegistry.componentType<A>()
    val storageA = storage(typeA)
    for (archetype in matchArchetypes(this, typeA)) {
        for (entity in entitiesIn(archetype)) {
            val a = storageA[entity.index] ?: continue
            action(entity, a)
        }
    }
}

/**
 * Iterates every entity that has [A] and [B], invoking [action] with the entity
 * and its components. Entities whose [A] or [B] storage slot is null are skipped.
 *
 * ```
 * world.query<FuelRodComponent, PositionComponent> { entity, rod, pos -> ... }
 * ```
 */
inline fun <reified A : Component, reified B : Component> World.query(
    noinline action: (Entity, A, B) -> Unit
) {
    val typeA = ComponentTypeRegistry.componentType<A>()
    val typeB = ComponentTypeRegistry.componentType<B>()
    val storageA = storage(typeA)
    val storageB = storage(typeB)
    for (archetype in matchArchetypes(this, typeA, typeB)) {
        for (entity in entitiesIn(archetype)) {
            val a = storageA[entity.index] ?: continue
            val b = storageB[entity.index] ?: continue
            action(entity, a, b)
        }
    }
}

/**
 * Iterates every entity that has [A], [B], and [C], invoking [action] with the
 * entity and its components. Entities whose [A], [B], or [C] storage slot is null
 * are skipped.
 *
 * ```
 * world.query<FuelRodComponent, PositionComponent, ThermalPropertiesComponent> { entity, rod, pos, therm -> ... }
 * ```
 */
inline fun <reified A : Component, reified B : Component, reified C : Component> World.query(
    noinline action: (Entity, A, B, C) -> Unit
) {
    val typeA = ComponentTypeRegistry.componentType<A>()
    val typeB = ComponentTypeRegistry.componentType<B>()
    val typeC = ComponentTypeRegistry.componentType<C>()
    val storageA = storage(typeA)
    val storageB = storage(typeB)
    val storageC = storage(typeC)
    for (archetype in matchArchetypes(this, typeA, typeB, typeC)) {
        for (entity in entitiesIn(archetype)) {
            val a = storageA[entity.index] ?: continue
            val b = storageB[entity.index] ?: continue
            val c = storageC[entity.index] ?: continue
            action(entity, a, b, c)
        }
    }
}

/**
 * Iterates every entity that has [A], [B], [C], and [D], invoking [action] with
 * the entity and its components. Entities whose [A], [B], [C], or [D] storage
 * slot is null are skipped.
 *
 * ```
 * world.query<FuelRodComponent, PositionComponent, ThermalPropertiesComponent, CoolantChannelComponent> { entity, rod, pos, therm, coolant -> ... }
 * ```
 */
inline fun <
    reified A : Component,
    reified B : Component,
    reified C : Component,
    reified D : Component
    > World.query(
    noinline action: (Entity, A, B, C, D) -> Unit
) {
    val typeA = ComponentTypeRegistry.componentType<A>()
    val typeB = ComponentTypeRegistry.componentType<B>()
    val typeC = ComponentTypeRegistry.componentType<C>()
    val typeD = ComponentTypeRegistry.componentType<D>()
    val storageA = storage(typeA)
    val storageB = storage(typeB)
    val storageC = storage(typeC)
    val storageD = storage(typeD)
    for (archetype in matchArchetypes(this, typeA, typeB, typeC, typeD)) {
        for (entity in entitiesIn(archetype)) {
            val a = storageA[entity.index] ?: continue
            val b = storageB[entity.index] ?: continue
            val c = storageC[entity.index] ?: continue
            val d = storageD[entity.index] ?: continue
            action(entity, a, b, c, d)
        }
    }
}

/**
 * Archetypes in [world] that contain every one of [types].
 *
 * Called by the public inline [query] functions above. Those bodies are inlined
 * into other files, so Kotlin's inline-visibility rule requires the helper to be
 * reachable from there: it is `internal` with [PublishedApi] rather than
 * `private`, which keeps it off the public API surface while still letting
 * inlined call sites reach it. Its logic is unchanged from the original.
 */
@PublishedApi
internal fun matchArchetypes(world: World, vararg types: ComponentType<*>): List<Archetype> {
    val required = types.toList()
    val result = ArrayList<Archetype>()
    for (archetype in world.archetypes()) {
        if (archetype.containsAll(required)) {
            result.add(archetype)
        }
    }
    return result
}
