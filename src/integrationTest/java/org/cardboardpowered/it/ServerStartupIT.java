/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.it;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration test scaffold. Never executed by {@code gradlew test}: it lives in
 * the {@code integrationTest} source set and is discoverable only through
 * {@code gradlew integrationTest}, which a human runs after authorizing a real
 * Fabric 1.21.11 server start.
 */
@Tag("integration")
@Disabled("Requires a real Fabric 1.21.11 server with Cardboard; manual run only.")
class ServerStartupIT {

    // Manual run checklist for the future harness:
    // 1. The Fabric 1.21.11 server boots with Cardboard + Fabric API.
    // 2. A plugins/ directory is auto-created next to the server jar.
    // 3. Cardboard config files are generated under config/cardboard/.
    // 4. CraftServer finishes initialization (look for the completion log marker).
    // 5. A Libby-based plugin (Citizens) enables without --add-opens and fully loads.
    // 6. Graceful stop exits 0 with no lingering non-daemon threads.
    @Test
    void serverStartsAndCreatesPluginEnvironment() {
        // Intentionally empty until wired to a real server harness.
    }
}
