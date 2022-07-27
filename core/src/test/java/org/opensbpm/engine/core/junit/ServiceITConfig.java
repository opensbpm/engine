/** *****************************************************************************
 * Copyright (C) 2020 Stefan Sedelmaier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************
 */
package org.opensbpm.engine.core.junit;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opensbpm.engine.api.events.EngineEvent;
import org.opensbpm.engine.core.EngineConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

/**
 * Spring-Boot Configuration for JPA-related Service-Layer Integration-Tests.
 */
@Import(EngineConfig.class)
@Configuration
public class ServiceITConfig {

    @Bean
    public EngineEventsCollector engineEventsCollector() {
        return new EngineEventsCollector();
    }

    public static class EngineEventsCollector extends AbstractCollection<EngineEvent<? extends Serializable>> {

        private List<EngineEvent<? extends Serializable>> events = new ArrayList<>();

        @EventListener
        public void handleEngineEvent(EngineEvent<? extends Serializable> engineEvent) {
            events.add(engineEvent);
        }

        @Override
        public int size() {
            return events.size();
        }

        @Override
        public Iterator<EngineEvent<? extends Serializable>> iterator() {
            return events.iterator();
        }

    }

}
