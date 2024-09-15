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
package org.opensbpm.engine.core.model.entities;

import jakarta.persistence.Entity;

@Entity
public class ServiceSubjectModel extends SubjectModel {

    protected ServiceSubjectModel() {
    }

    public ServiceSubjectModel(String name) {
        super(name);
    }

    @Override
    public <T> T accept(SubjectModelVisitor<T> visitor) {
        return visitor.visitServiceSubjectModel(this);
    }

}
