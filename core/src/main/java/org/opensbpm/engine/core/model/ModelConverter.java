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
package org.opensbpm.engine.core.model;

import static org.opensbpm.engine.utils.StreamUtils.mapToList;

import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelInfo.SubjectModelInfo;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import java.util.Collection;
import java.util.List;

public class ModelConverter {

    public static List<ProcessModelInfo> convertModels(Collection<ProcessModel> processModels) {
        return mapToList(processModels, ModelConverter::convertModel);
    }

    public static ProcessModelInfo convertModel(ProcessModel processModel) {
        return new ProcessModelInfo(processModel.getId(),
                processModel.getName(),
                processModel.getVersion().toString(),
                processModel.getDescription(),
                processModel.getState(),
                processModel.getCreatedAt(),
                convertSubjects(processModel.getUserSubjectModels()));
    }

    private static List<SubjectModelInfo> convertSubjects(Collection<UserSubjectModel> subjectModels) {
        return mapToList(subjectModels,
                subject -> new SubjectModelInfo(subject.getId(),
                        subject.getName(),
                        convertRoles(subject.getRoles()))
        );
    }

    private static List<String> convertRoles(Collection<Role> roles) {
        return mapToList(roles, Role::getName);
    }

    private ModelConverter() {
    }

}
