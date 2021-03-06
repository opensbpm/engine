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
 * ****************************************************************************
 */
package org.opensbpm.engine.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static java.util.Arrays.asList;

public class StreamUtils {

    /**
     * create a {@link Collections#unmodifiableList(java.util.List) } with the given {@link List}, if the given
     * {@link List} is {@code null} a {@link Collections#emptyList()} is returned. Its a shortcut for
     * <blockquote><pre>
     *  list == null
     *      ? Collections.emptyList()
     *      : Collections.unmodifiableList(list)
     * </pre></blockquote>
     *
     * @param <T> type of List
     * @param nullableList can be {@code null}
     * @return return an {@link Collections#unmodifiableList(java.util.List)}, if given list is {@code null} an
     * {@link Collections#emptyList()} will be returned
     */
    public static <T> List<T> emptyOrUnmodifiableList(List<T> nullableList) {
        return Optional.ofNullable(nullableList)
                .map(t -> Collections.unmodifiableList(t))
                .orElse(Collections.emptyList());
    }

    /**
     * create a {@link Collections#unmodifiableSet(java.util.Set) } with the given {@link Set}, if the given {@link Set}
     * is {@code null} a {@link Collections#emptySet()} is returned. Its a shortcut for
     * <blockquote><pre>
     *  set == null
     *      ? Collections.emptySet()
     *      : Collections.unmodifiableSet(set)
     * </pre></blockquote>
     *
     * @param <T> type of Set
     * @param nullableSet can be {@code null}
     * @return return an {@link Collections#unmodifiableSet(java.util.Set)}, if given set is {@code null} an
     * {@link Collections#emptySet()} will be returned
     */
    public static <T> Set<T> emptyOrUnmodifiableSet(Set<T> nullableSet) {
        return Optional.ofNullable(nullableSet)
                .map(t -> Collections.unmodifiableSet(t))
                .orElse(Collections.emptySet());
    }

    /**
     * create a {@link Collections#unmodifiableMap(java.util.Map) } with the given {@link Map}, if the given {@link Map}
     * is {@code null} a {@link Collections#emptyMap()} is returned. Its a shortcut for
     * <blockquote><pre>
     *  map == null
     *      ? Collections.emptyMap()
     *      : Collections.unmodifiableMap(map)
     * </pre></blockquote>
     *
     * @param <K> key of Map
     * @param <V> value of Map
     * @param nullableMap can be {@code null}
     * @return return an {@link Collections#unmodifiableMap(java.util.Map)}, if given Map is {@code null} an
     * {@link Collections#emptyMap()} will be returned
     */
    public static <K, V> Map<K, V> emptyOrUnmodifiableMap(Map<K, V> nullableMap) {
        return Optional.ofNullable(nullableMap)
                .map(t -> Collections.unmodifiableMap(t))
                .orElse(Collections.emptyMap());
    }

    /**
     * Add the given elements to a nullable list. if the list is null a new list is created.
     *
     * @param <T> Type of the given list, the elements to add and the returning list
     * @param nullableList the nullable list
     * @param element mandatory element
     * @param elements optional elements
     * @return a non null list with the given elements
     */
    public static <T> List<T> lazyAdd(List<T> nullableList, T element, T... elements) {
        final List<T> list = Optional.ofNullable(nullableList)
                .orElse(new ArrayList<>());
        Collections.addAll(list, safeVarargs(element, elements));
        return list;
    }

    /**
     * Add the given elements to a nullable set. if the set is null a new set is created.
     *
     * @param <T> Type of the given set, the elements to add and the returning set
     * @param nullableSet the nullable set
     * @param element mandatory element
     * @param elements optional elements
     * @return a non null list with the given elements
     */
    public static <T> Set<T> lazyAdd(Set<T> nullableSet, T element, T... elements) {
        final Set<T> list = Optional.ofNullable(nullableSet)
                .orElse(new HashSet<>());
        Collections.addAll(list, safeVarargs(element, elements));
        return list;
    }

    public static <T> BinaryOperator<T> toOne() {
        return (t, u) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    /**
     * Reduce the given iterable with the given filter to one element. If there are
     * duplicate elements after the use of the filter an {@link IllegalStateException}
     * will be thrown.
     *
     * @param <T>
     * @param from
     * @param filter
     * @return
     */
    public static <T> Optional<T> filterToOne(Iterable<T> from, Predicate<? super T> filter) {
        return filterToOne(asStream(from), filter);
    }

    /**
     * Reduce the given stream with the given filter to one element. If there are
     * duplicate elements after the use of the filter an {@link IllegalStateException}
     * will be thrown.
     *
     * @param <T>
     * @param from
     * @param filter
     * @return
     */
    public static <T> Optional<T> filterToOne(Stream<T> from, Predicate<? super T> filter) {
        return from.filter(filter)
                .reduce(toOne());
    }

    private static <T> Stream<T> asStream(Iterable<T> from) {
        return StreamSupport.stream(from.spliterator(), false);
    }

    @SafeVarargs
    public static <T> T[] safeVarargs(T arg1, T... varargs) {
        Objects.requireNonNull(arg1);
        Objects.requireNonNull(varargs);

        //This cast is correct because the array we're creating 
        //is of the same type as arg1
        @SuppressWarnings("unchecked")
        final T[] args = (T[]) new Object[1 + varargs.length];
        args[0] = arg1;
        System.arraycopy(varargs, 0, args, 1, varargs.length);
        return args;
    }

    @SafeVarargs
    public static <T> List<T> oneOrMoreAsList(T arg1, T... varargs) {
        return asList(safeVarargs(arg1, varargs));
    }

    private StreamUtils() {
    }

}
