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
package org.opensbpm.engine.api;

import org.opensbpm.engine.api.SearchFilter.Criteria.Operation;
import org.opensbpm.engine.api.SearchFilter.SearchFilterBuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;


import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchFilterTest {

    @Test
    public void testValueOf() {
        //given
        //when
        final SearchFilter result = SearchFilter.valueOf("a=3,b>4");
        //then
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testToString() {
        //given
        final SearchFilter searchFilter = new SearchFilterBuilder()
                .with("a", Operation.LESSTHAN, "x")
                .with("b", Operation.EQUALTO, "x")
                .build();
        //when
        String result = searchFilter.toString();

        //then
        assertThat(result, is("a<x,b:x"));
    }

}
