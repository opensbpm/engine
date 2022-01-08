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
 * ****************************************************************************
 */
package org.opensbpm.engine.core.model;

import static org.opensbpm.engine.utils.StreamUtils.mapToMap;

import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.builder.ProcessBuilder;
import org.opensbpm.engine.api.model.builder.ServiceSubjectBuilder;
import org.opensbpm.engine.api.model.builder.StateBuilder;
import org.opensbpm.engine.api.model.builder.SubjectBuilder;
import org.opensbpm.engine.api.model.builder.UserSubjectBuilder;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.model.ObjectModelConverter.ObjectModelCache;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.SubjectModelVisitor;

import static org.opensbpm.engine.core.model.entities.SubjectModelVisitor.userSubjectModel;

import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProcessModelConverter {

    public static ProcessDefinition convert(ProcessModel processModel) {
        return new ProcessModelConverter(processModel).create();
    }

    private final ProcessModel processModel;
    private final ProcessBuilder processBuilder;
    private ObjectModelCache objectModelCache;
    private Map<SubjectModel, SubjectBuilder<?, ?>> subjects;

    private ProcessModelConverter(ProcessModel processModel) {
        this.processModel = processModel;
        processBuilder = new ProcessBuilder(processModel.getName())
                .description(processModel.getDescription())
                .version(processModel.getVersion().getMajor());
        if (ProcessModelState.INACTIVE == processModel.getState()) {
            processBuilder.asIncative();
        }
    }

    private ProcessDefinition create() {
        objectModelCache = new ObjectModelConverter().convert(processModel);
        objectModelCache.getObjectBuilders()
                .forEach(objectBuilder -> processBuilder.addObject(objectBuilder));

        subjects = createSubjects();
        subjects.values().forEach(subjectBuilder
                -> processBuilder.addSubject(subjectBuilder));

        StateModelConverter stateConverter = new StateModelConverter(objectModelCache, subjects);
        subjects.forEach((subjectModel, subjectBuilder)
                -> {
            Map<State, StateBuilder<?, ?>> states = mapToMap(subjectModel.getStates(),
                    state -> stateConverter.createStateBuilder(state));
            stateConverter.createStateGraph(subjectModel, states);

            states.values().forEach(stateBuilder
                    -> subjectBuilder.addState(stateBuilder));
        });

        return processBuilder.build();
    }

    private Map<SubjectModel, SubjectBuilder<?, ?>> createSubjects() {
        return mapToMap(processModel.getSubjectModels(), subjectModel -> {
            SubjectBuilder<?, ?> subjectBuilder = subjectModel.accept(new SubjectModelVisitor<SubjectBuilder<?, ?>>() {
                @Override
                public SubjectBuilder<?, ?> visitUserSubjectModel(UserSubjectModel userSubjectModel) {
                    List<String> roles = getUserSubjectModelRoles(subjectModel).stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toList());
                    return new UserSubjectBuilder(subjectModel.getName(), roles);
                }

                @Override
                public SubjectBuilder<?, ?> visitServiceSubjectModel(ServiceSubjectModel serviceSubjectModel) {
                    return new ServiceSubjectBuilder(subjectModel.getName());
                }
            });
            if (processModel.isStarterSubjectModel(subjectModel)) {
                subjectBuilder.asStarter();
            }
            return subjectBuilder;
        });
    }

    /**
     * get all {@link Role}s from given {@link SubjectModel}.
     *
     * @param subjectModel
     * @return {@link UserSubjectModel#getRoles()} or
     * {@link Collections#emptyList()} if given {@link SubjectModel} is type of
     * {@link ServiceSubjectModel}
     * @throws NullPointerException if subjectModel is <code>null</code>
     */
    private static Collection<Role> getUserSubjectModelRoles(SubjectModel subjectModel) {
        Objects.requireNonNull(subjectModel, "subjectModel must not be null");
        return subjectModel.accept(userSubjectModel())
                .map(userSubjectModel -> userSubjectModel.getRoles())
                .orElse(Collections.emptyList());
    }

}
