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
package org.opensbpm.engine.api.junit;

import org.opensbpm.engine.api.instance.AuditTrail;

import static org.opensbpm.engine.api.junit.CommonMatchers.value;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;

import org.hamcrest.Matcher;

public final class AuditTrailMatchers {

    public static Matcher<AuditTrail> isTrail(String subjectName, String stateName) {
        return allOf(isSubjectName(subjectName), isStateName(stateName));
    }

    public static Matcher<AuditTrail> isTrail(String subjectName, String userName, String stateName) {
        return allOf(isSubjectName(subjectName), isUserName(userName), isStateName(stateName));
    }

    private static Matcher<AuditTrail> isSubjectName(String name) {
        return value(AuditTrail::getSubjectName, is(name));
    }

    private static Matcher<AuditTrail> isUserName(String name) {
        return value((t) -> t.getUser().getName(), is(name));
    }

    private static Matcher<AuditTrail> isStateName(String name) {
        return value(AuditTrail::getStateName, is(name));
    }

    private AuditTrailMatchers() {
    }

}
