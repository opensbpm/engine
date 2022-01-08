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

import java.util.Optional;

public interface SubjectVisitor<T> {

    T visitUserSubject(UserSubject userSubject);

    T visitServiceSubject(ServiceSubject serviceSubject);

    static SubjectVisitor<Optional<UserSubject>> userSubject() {
        return new OptionalSubjectAdapter<UserSubject>() {
            @Override
            public Optional<UserSubject> visitUserSubject(UserSubject subject) {
                return Optional.of(subject);
            }
        };
    }

    static SubjectVisitor<Optional<ServiceSubject>> serviceSubject() {
        return new OptionalSubjectAdapter<ServiceSubject>() {
            @Override
            public Optional<ServiceSubject> visitServiceSubject(ServiceSubject subject) {
                return Optional.of(subject);
            }
        };
    }

    public static class OptionalSubjectAdapter<T> implements SubjectVisitor<Optional<T>> {

        @Override
        public Optional<T> visitUserSubject(UserSubject userSubject) {
            return Optional.empty();
        }

        @Override
        public Optional<T> visitServiceSubject(ServiceSubject serviceSubject) {
            return Optional.empty();
        }

    }

}
