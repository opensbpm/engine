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
package org.opensbpm.engine.core.junit;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

class AssocationHelper {

    /**
     *
     * @param <Z> the source type of the relationship
     * @param <X> the target type of the relationship
     */
    static class RelationshipAccessor<Z, X> {

        protected final Class<Z> sourceType;
        protected final Z source;
        private final String property;
        private final Class<X> targetType;

        protected RelationshipAccessor(Class<Z> sourceType, Z source, String property, Class<X> targetType) {
            this.sourceType = sourceType;
            this.source = source;
            this.property = property;
            this.targetType = targetType;
        }

        public final Class<Z> getSourceType() {
            return sourceType;
        }

        public final String getProperty() {
            return property;
        }

        protected final Method getMethod(String name, Class<?>... parameterTypes) throws ReflectiveOperationException {
            Method method;
            try {
                method = sourceType.getMethod(name, parameterTypes);
                method.setAccessible(true);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "{0} has no {1}", new Object[]{sourceType.getName(), name/*, ex*/});
                method = null;
            }
            return method;
        }

        protected static String capitalize(String name) {
            char chars[] = name.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        }
    }

    /**
     *
     * @param <Z> the source type of the relationship
     * @param <X> the target type of the relationship
     */
    static class ToManyAccessor<Z, X> extends RelationshipAccessor<Z, X> {

        //
        private final Method getManyMethod;
        private final Method addMethod;
        private final Method removeMethod;

        public ToManyAccessor(Class<Z> sourceType, Z source, String property, Class<X> targetType) throws ReflectiveOperationException {
            super(sourceType, source, property, targetType);

            getManyMethod = getMethod("get" + capitalize(property) + "s");
            addMethod = getMethod("add" + capitalize(property), targetType);
            removeMethod = getMethod("remove" + capitalize(property), targetType);
        }

        boolean hasGetMethod() {
            return getManyMethod != null;
        }

        public Method getGetManyMethod() {
            return getManyMethod;
        }

        public Collection<X> getMany() throws ReflectiveOperationException {
            return (Collection<X>) getManyMethod.invoke(source);
        }

        boolean hasAddMethod() {
            return addMethod != null;
        }

        public void add(X target) throws ReflectiveOperationException {
            addMethod.invoke(source, target);
        }

        boolean hasRemoveMethod() {
            return removeMethod != null;
        }

        public void remove(X target) throws ReflectiveOperationException {
            removeMethod.invoke(source, target);
        }

    }

    static class ToOneAccessor<Z, X> extends RelationshipAccessor<Z, X> {

        private final Method getMethod;
        private final Method setMethod;

        public ToOneAccessor(Class<Z> sourceType, Z source, String property, Class<X> targetType) throws ReflectiveOperationException {
            super(sourceType, source, property, targetType);

            getMethod = getMethod("get" + capitalize(property));
            setMethod = getMethod("set" + capitalize(property), targetType);
        }

        public boolean hasGetOneMethod() {
            return getMethod != null;
        }

        public X getOne() throws ReflectiveOperationException {
            return (X) getMethod.invoke(source);
        }

        public boolean hasSetOneMethod() {
            return setMethod != null;
        }

        public void setOne(X target) throws ReflectiveOperationException {
            setMethod.invoke(source, target);
        }
    }

}
