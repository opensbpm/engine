/*******************************************************************************
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
 ******************************************************************************/
package org.opensbpm.engine.api.model.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import static org.opensbpm.engine.utils.StreamUtils.mapToList;

public class ProcessBuilder extends AbstractBuilder<ProcessDefinition,ProcessBuilder> {

    private final String name;
    private String description;
    private int version;
    private ProcessModelState state = ProcessModelState.ACTIVE;
    private final Map<String, SubjectBuilder<?, ?>> subjectBuilders = new LinkedHashMap<>();
    private final Map<String, ObjectBuilder> objectBuilders = new LinkedHashMap<>();

    public ProcessBuilder(String name) {
        this.name = name;
    }

    @Override
    protected ProcessBuilder self() {
        return this;
    }

    
    public ProcessBuilder description(String description) {
        checkBuilt();
        this.description = description;
        return self();
    }

    public ProcessBuilder version(int version) {
        checkBuilt();
        this.version = version;
        return self();
    }

    public ProcessBuilder asIncative() {
        checkBuilt();
        this.state = ProcessModelState.INACTIVE;
        return self();
    }

    public ProcessBuilder addSubject(SubjectBuilder<?, ?> subjectBuilder) {
        checkBuilt();
        if (!subjectBuilders.containsKey(subjectBuilder.getName())) {
            subjectBuilders.put(subjectBuilder.getName(), subjectBuilder);
        }
        return self();
    }

    public SubjectBuilder<?, ?> getSubject(String name) {
        return subjectBuilders.get(name);
    }

    public ProcessBuilder addObject(ObjectBuilder objectBuilder) {
        checkBuilt();
        objectBuilders.put(objectBuilder.getName(), objectBuilder);
        return self();
    }

    public ObjectBuilder getObject(String name) {
        if (!objectBuilders.containsKey(name)) {
            throw new IllegalArgumentException("ObjectBuilder " + name + " not found");
        }
        return objectBuilders.get(name);
    }

    @Override
    protected ProcessDefinition create() {
        List<ObjectDefinition> objects = mapToList(objectBuilders.values(), AbstractBuilder::build);

        List<SubjectDefinition> subjects = mapToList(subjectBuilders.values(), SubjectBuilder::build);

        //avoid stackoverflow and update sendstate-receiver as last
        subjectBuilders.values().stream()
                .flatMap(subjectBuilder
                        -> subjectBuilder.getAllStateBuilders().stream()
                        .filter(SendStateBuilder.class::isInstance)
                        .map(SendStateBuilder.class::cast))
                .forEach((builder) -> builder.updateReceiver());

        ProcessDefinition processDefinition = new ProcessDefinition() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public int getVersion() {
                return version;
            }

            @Override
            public ProcessModelState getState() {
                return state;
            }

            @Override
            public List<SubjectDefinition> getSubjects() {
                return subjects;
            }

            @Override
            public List<ObjectDefinition> getObjects() {
                return objects;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .append("name", name)
                        .append("subjects", subjects)
                        .append("objects", objects)
                        .toString();
            }

        };

        validateDefinition(processDefinition);
        return processDefinition;
    }

    private void validateDefinition(ProcessDefinition processDefinition) {
        processDefinition.getSubjects().forEach(subject -> {
            subject.getStates().forEach(state -> {
                if (state.getHeads().isEmpty() && StateEventType.END != state.getEventType()) {
                    throw new IllegalStateException("empty heads and not End state: " + state);
                }
            });
        });
    }
}
