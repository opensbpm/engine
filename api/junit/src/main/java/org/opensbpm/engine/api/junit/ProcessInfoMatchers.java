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

import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo.StateFunctionType;
import org.opensbpm.engine.api.instance.ProcessInstanceState;

import static org.opensbpm.engine.api.junit.CommonMatchers.value;

import org.opensbpm.engine.api.instance.UserToken;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.hasItems;

public final class ProcessInfoMatchers {

    public static Matcher<ProcessInfo> isState(ProcessInstanceState state) {
        return value(ProcessInfo::getState, is(state));
    }

    public static Matcher<Iterable<SubjectStateInfo>> hasSubjects(Matcher<SubjectStateInfo>... subjectStates) {
        return hasItems(subjectStates);
    }

    public static Matcher<ProcessInfo> hasSubjects(Matcher<Iterable<SubjectStateInfo>> subjectsMatcher) {
        return value(ProcessInfo::getSubjects, subjectsMatcher);
    }

    public static Matcher<SubjectStateInfo> isSubjectState(String subjectName, String stateName, StateFunctionType functionType) {
        return isStateFunction(subjectName, stateName, functionType);
    }

    public static Matcher<SubjectStateInfo> isUserSubjectState(String userName, String subjectName, String stateName, StateFunctionType functionType) {
        return isUserStateFunction(userName, subjectName, stateName, functionType);
    }

    private static Matcher<SubjectStateInfo> isStateFunction(String subjectName, String stateName, StateFunctionType functionType) {
        return allOf(isSubjectName(subjectName), isStateName(stateName), isFunctionType(functionType));
    }

    private static Matcher<SubjectStateInfo> isUserStateFunction(String userName, String subjectName, String stateName, StateFunctionType functionType) {
        return allOf(isUser(userName), isSubjectName(subjectName), isStateName(stateName), isFunctionType(functionType));
    }

    private static Matcher<SubjectStateInfo> isUser(String userName) {
        return value(SubjectStateInfo::getUser, isUserName(userName));
    }

    public static Matcher<UserToken> isUserId(Long id) {
        return value("userDefinition.id", UserToken::getId, is(id));
    }

    public static Matcher<UserToken> isUserName(String userName) {
        return value("userDefinition.name", UserToken::getName, is(userName));
    }

    private static Matcher<SubjectStateInfo> isSubjectName(String subjectName) {
        return value(SubjectStateInfo::getSubjectName, is(subjectName));
    }

    private static Matcher<SubjectStateInfo> isStateName(String stateName) {
        return value(SubjectStateInfo::getStateName, is(stateName));
    }

    private static Matcher<SubjectStateInfo> isFunctionType(StateFunctionType functionType) {
        return value(SubjectStateInfo::getStateFunctionType, is(functionType));
    }

    private ProcessInfoMatchers() {
    }

}
