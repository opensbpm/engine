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
package org.opensbpm.engine.core.utils.entities;

import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class HasIdTest {

    @Test
    public void testEqualsIdWithNull() {
        //given
        HasId hasId1 = () -> {
            throw new UnsupportedOperationException("Not supported yet.");
        };

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> hasId1.equalsId(null));

        //then
        assertThat("equalsId(null) must throw NullPointerException",
                exception.getMessage(), is("other must not be null"));
    }

    @Ignore("Typecheck doesn't work Hibernate Proxies")
    @Test
    public void testEqualsIdWithDifferentClasses() {
        //given
        HasId hasId1 = () -> {
            throw new UnsupportedOperationException("Not supported yet.");
        };
        HasId hasId2 = () -> {
            throw new UnsupportedOperationException("Not supported yet.");
        };

        //when
        boolean equalsId = hasId1.equalsId(hasId2);

        //then
        assertThat("two different types of HasId must not equals", equalsId, is(false));
    }

    @Test
    public void testEqualsIdWithNullId() {
        //given
        HasId hasId = () -> {
            return null;
        };

        //when
        boolean equalsId = hasId.equalsId(hasId);

        //then
        assertThat("HasId with null id must not equals", equalsId, is(false));
    }

    @Test
    public void testEqualsIdWithDifferentIds() {
        //given
        HasId hasId1 = new TestHasId(1);
        HasId hasId2 = new TestHasId(2);

        //when
        boolean equalsId = hasId1.equalsId(hasId2);

        //then
        assertThat("HasId with different id must not equals", equalsId, is(false));
    }

    @Test
    public void testEqualsIdWithSameIds() {
        //given
        HasId hasId1 = new TestHasId(1);
        HasId hasId2 = new TestHasId(1);

        //when
        boolean equalsId = hasId1.equalsId(hasId2);

        //then
        assertThat("HasId with same ids must equals", equalsId, is(true));
    }

    private static class TestHasId implements HasId {

        private final long id;

        public TestHasId(long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

}
