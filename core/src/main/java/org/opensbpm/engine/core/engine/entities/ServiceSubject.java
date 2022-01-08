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
package org.opensbpm.engine.core.engine.entities;

import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.opensbpm.engine.core.model.entities.SubjectModel;

import static org.opensbpm.engine.core.model.entities.SubjectModelVisitor.serviceSubjectModel;

import javax.persistence.Entity;

@Entity(name = "subjectservice")
public class ServiceSubject extends Subject {

    protected ServiceSubject() {
    }

    public ServiceSubject(ProcessInstance processInstance, SubjectModel subjectModel) {
        super(processInstance, subjectModel);
    }

    @Override
    public ServiceSubjectModel getSubjectModel() {
        return subjectModel.accept(serviceSubjectModel())
                .orElseThrow(() -> new IllegalStateException(subjectModel + " not type of ServiceSubjectModel"));
    }

    @Override
    public <T> T accept(SubjectVisitor<T> visitor) {
        return visitor.visitServiceSubject(this);
    }
}
