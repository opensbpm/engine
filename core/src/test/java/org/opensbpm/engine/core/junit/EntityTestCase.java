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
package org.opensbpm.engine.core.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.junit.Test;
import org.opensbpm.engine.core.junit.AssocationHelper.ToManyAccessor;
import org.opensbpm.engine.core.junit.AssocationHelper.ToOneAccessor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public abstract class EntityTestCase<E> {

    private final Class<E> entityClass;

    private static <T> T instantiate(Class<T> targetType) throws ReflectiveOperationException {
        Constructor<T> constructor = targetType.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    protected EntityTestCase(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @Test
    public void testOneToMany() throws ReflectiveOperationException {
        testAssociation(Association.OneToMany, field -> {
            testOneToMany(field.getName(), field.getType());
        });
    }

    @Test
    public void testManyToOne() throws ReflectiveOperationException {
        testAssociation(Association.ManyToOne, field -> {
            testManyToOne(field.getName(), field.getType());
        });
    }

    private void testAssociation(Association association, TestFunction testFunction) throws ReflectiveOperationException, SecurityException {
        if (Modifier.isAbstract(entityClass.getModifiers())) {
            LOGGER.log(Level.WARNING, "skip testOneToMany, entity {0} is abstract", entityClass.getName());
        } else {
            for (Field field : entityClass.getDeclaredFields()) {
                Annotation[] annotations = field.getDeclaredAnnotationsByType(association.getAnnotation());
                if (annotations != null && annotations.length > 0) {
                    if (Modifier.isAbstract(field.getType().getModifiers())) {
                        LOGGER.log(Level.WARNING, "skip testOneToMany({0}), target-type {1} is abstract", new Object[]{field.getName(), field.getType().getName()});
                    } else {
                        testFunction.execute(field);
                    }
                }
            }
        }
    }
    private Logger LOGGER = Logger.getLogger(getClass().getName());

    private <X> void testOneToMany(String property, Class<X> type) throws ReflectiveOperationException {
        ToManyAccessor<E, X> accessor = createToManyAccessor(property, type);
        if (accessor.hasGetMethod()) {
            testGetOneToMany(accessor, type);
            if (accessor.hasAddMethod() && accessor.hasRemoveMethod()) {
                testAddOneToMany(property, type);
                testRemoveOneToMany(property, type);
            }
        } else {
            LOGGER.warning("no Methods to execute test");
        }
    }

    protected <X> void testGetOneToMany(String property, Class<X> targetType) throws ReflectiveOperationException {
        testGetOneToMany(property, targetType, targetType);
    }

    protected <X> void testGetOneToMany(String property, Class<X> targetType, Class<? extends X> implType) throws ReflectiveOperationException {
        ToManyAccessor<E, X> accessor = createToManyAccessor(property, targetType);
        testGetOneToMany(accessor, implType);
    }

    private <X> void testGetOneToMany(ToManyAccessor<E, X> accessor, Class<? extends X> implType) throws ReflectiveOperationException {
        assertThat(accessor.getMany(), is(notNullValue()));
        Collection<X> collection = accessor.getMany();
        X toMany = instantiate(implType);
        try {
            collection.add(toMany);
            fail(accessor.getGetManyMethod() + " must return unmodifiable Collection");
        } catch (Exception ex) {
            String message = accessor.getGetManyMethod() + " must not throw NullPointerException";
            assertThat(message, ex, not(instanceOf(NullPointerException.class)));
        }
    }

    protected <X> void testAddOneToMany(String property, Class<X> targetType) throws ReflectiveOperationException {
        testAddOneToMany(property, targetType, targetType);
    }

    protected <X> void testAddOneToMany(String property, Class<X> targetType, Class<? extends X> implType) throws ReflectiveOperationException {
        ToManyAccessor<E, X> invoker = createToManyAccessor(property, targetType);

        X toMany = instantiate(implType);
        invoker.add(toMany);
        assertThat(invoker.getMany(), hasItem(toMany));

        invoker.add(toMany);
        assertThat(invoker.getMany(), hasItem(toMany));

    }

    protected <X> void testRemoveOneToMany(String property, Class<X> targetType) throws ReflectiveOperationException {
        testRemoveOneToMany(property, targetType, targetType);
    }

    protected <X> void testRemoveOneToMany(String property, Class<X> targetType, Class<? extends X> implType) throws ReflectiveOperationException {
        ToManyAccessor<E, X> invoker = createToManyAccessor(property, targetType);

        X toMany = instantiate(implType);
        invoker.remove(toMany);
        assertThat(invoker.getMany(), not(hasItem(toMany)));

        invoker.add(toMany);
        assertThat(invoker.getMany(), hasItem(toMany));

        invoker.remove(toMany);
        assertThat(invoker.getMany(), not(hasItem(toMany)));

    }

    protected <X> void testManyToOne(String property, Class<X> targetType) throws ReflectiveOperationException {
        ToOneAccessor<E, X> accessor = createToOneAccessor(property, targetType);
        if (accessor.hasGetOneMethod()) {
            testGetManyToOne(accessor, targetType);
            if (accessor.hasSetOneMethod()) {
                testSetManyToOne(accessor, targetType);
            }
        } else {
            LOGGER.warning("no Methods to execute test");
        }

    }

    protected <X> void testSetManyToOne(String property, Class<X> targetType, Class<? extends X> implType) throws ReflectiveOperationException {
        ToOneAccessor<E, X> invoker = createToOneAccessor(property, targetType);
        testSetManyToOne(invoker, implType);
    }

    private <X> void testSetManyToOne(ToOneAccessor<E, X> invoker, Class<? extends X> implType) throws ReflectiveOperationException {
        X toMany = instantiate(implType);
        invoker.setOne(toMany);
        assertThat(invoker.getOne(), is(toMany));

        invoker.setOne(toMany);
        assertThat(invoker.getOne(), is(toMany));

        toMany = instantiate(implType);
        invoker.setOne(toMany);
        assertThat(invoker.getOne(), is(toMany));
    }

    private <X> void testGetManyToOne(ToOneAccessor<E, X> invoker, Class<X> targetType) throws ReflectiveOperationException {
        assertThat(invoker.getOne(), is(nullValue()));

        if (invoker.hasSetOneMethod()) {
            X toMany = instantiate(targetType);
            invoker.setOne(toMany);
            assertThat(invoker.getOne(), is(toMany));
        }
    }

    private <X> ToOneAccessor<E, X> createToOneAccessor(String property, Class<X> targetType) throws ReflectiveOperationException {
        E instance = instantiate(entityClass);
        return new ToOneAccessor<>(entityClass, instance, property, targetType);
    }

    private <X> ToManyAccessor<E, X> createToManyAccessor(String property, Class<X> targetType) throws ReflectiveOperationException {
        E instance = instantiate(entityClass);
        return new ToManyAccessor<>(entityClass, instance, property, targetType);
    }

    private enum Association {
        OneToMany(OneToMany.class), ManyToOne(ManyToOne.class);

        final Class<? extends Annotation> annotation;

        private Association(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }

        public Class<? extends Annotation> getAnnotation() {
            return annotation;
        }

    }

    @FunctionalInterface
    interface TestFunction {

        void execute(Field field) throws ReflectiveOperationException;
    }
}
