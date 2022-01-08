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
package org.opensbpm.engine.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.opensbpm.engine.api.SearchFilter.Criteria.Operation;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchFilter {

    public static class SearchFilterBuilder {

        private final List<Criteria> criterias = new ArrayList<>();

        public static SearchFilterBuilder create() {
            return new SearchFilterBuilder();
        }

        public SearchFilterBuilder with(String key, Operation operation, Object value) {
            criterias.add(new Criteria(key, operation, value));
            return this;
        }

        public SearchFilter build() {
            return new SearchFilter(criterias);
        }
    }

    private static final Pattern FILTER_PATTERN = Pattern.compile("(\\w+?)(:|<|>)(\\w+?),");

    public static SearchFilter valueOf(String filter) {
        SearchFilterBuilder builder = new SearchFilterBuilder();
        Matcher matcher = FILTER_PATTERN.matcher(filter + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1), Operation.valueOfOperator(matcher.group(2)), matcher.group(3));
        }
        return builder.build();
    }

    private final List<Criteria> criterias;

    SearchFilter(List<Criteria> criterias) {
        this.criterias = Collections.unmodifiableList(criterias);
    }

    public List<Criteria> getCriterias() {
        return Collections.unmodifiableList(criterias);
    }

    @Override
    public String toString() {
        return getCriterias().stream()
                .map(criteria
                        -> new StringBuilder().append(criteria.getKey())
                        .append(criteria.getOperation().getOperator())
                        .append(criteria.getValue())
                )
                .collect(Collectors.joining(","));
    }

    public static class Criteria {

        private final String key;
        private final Operation operation;
        private final Object value;

        Criteria(String key, Operation operation, Object value) {
            this.key = key;
            this.operation = operation;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Operation getOperation() {
            return operation;
        }

        public Object getValue() {
            return value;
        }

        public enum Operation {
            GREATERTHAN(">"),
            GREATERTHAN_EQUALTO(">="),
            LESSTHAN("<"),
            LESSTHAN_EQUALTO("<="),
            EQUALTO(":");

            private final String operator;

            private Operation(String operator) {
                this.operator = operator;
            }

            public String getOperator() {
                return operator;
            }

            public static Operation valueOfOperator(String operator) {
                for (Operation value : values()) {
                    if (value.operator.equals(operator)) {
                        return value;
                    }
                }
                throw new IllegalArgumentException("No enum constant for operator " + operator);
            }

        }
    }

}
