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
package org.opensbpm.engine.core.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelInfo.SubjectModelInfo;
import org.opensbpm.engine.api.model.ProcessModelInfo.SubjectModelInfo.RoleInfo;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;

public class ModelConverter {

    public static List<ProcessModelInfo> convertModels(Collection<ProcessModel> processModels) {
        return processModels.stream()
                .map(ModelConverter::convertModel)
                .collect(Collectors.toList());
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
        return subjectModels.stream()
                .map(ModelConverter::createSubjectModel)
                .collect(Collectors.toList());
    }

    private static SubjectModelInfo createSubjectModel(UserSubjectModel subject) {
        return new SubjectModelInfo(subject.getId(), subject.getName(), convertRoles(subject.getRoles()));
    }

    private static List<RoleInfo> convertRoles(Collection<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .map(RoleInfo::of)
                .collect(Collectors.toList());
    }

    private ModelConverter() {
    }

}
