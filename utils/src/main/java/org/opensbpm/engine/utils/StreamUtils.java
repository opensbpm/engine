package org.opensbpm.engine.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.tuple.Pair;
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

    public static <T, U> List<U> mapToList(Iterable<T> from, Function<T, U> mapper) {
        return mapToList(asStream(from), mapper);
    }

    public static <T, U> List<U> mapToList(Stream<T> from, Function<T, U> mapper) {
        return from.map(mapper)
                .collect(Collectors.toList());
    }

    public static <T, U> List<U> flatMapToList(Iterable<T> from, Function<? super T, ? extends Stream<? extends U>> mapper) {
        return flatMapToList(asStream(from), mapper);
    }

    public static <T, U> List<U> flatMapToList(Stream<T> from, Function<? super T, ? extends Stream<? extends U>> mapper) {
        return from.flatMap(mapper)
                .collect(Collectors.toList());
    }

    public static <T, U> Set<U> mapToSet(Iterable<T> from, Function<T, U> mapper) {
        return mapToSet(asStream(from), mapper);
    }

    public static <T, U> Set<U> mapToSet(Stream<T> from, Function<T, U> mapper) {
        return from.map(mapper)
                .collect(Collectors.toSet());
    }

    public static <T, U> Set<U> flatMapToSet(Iterable<T> from, Function<? super T, ? extends Stream<? extends U>> mapper) {
        return flatMapToSet(asStream(from), mapper);
    }

    public static <T, U> Set<U> flatMapToSet(Stream<T> from, Function<? super T, ? extends Stream<? extends U>> mapper) {
        return from.flatMap(mapper)
                .collect(Collectors.toSet());
    }

    public static <T, U> Map<T, U> mapToMap(Iterable<T> from, Function<T, U> mapper) {
        return mapToMap(asStream(from), mapper);
    }

    public static <T, U> Map<T, U> mapToMap(Stream<T> from, Function<T, U> mapper) {
        return from.map(t -> Pair.of(t, mapper.apply(t)))
                .collect(toMap());
    }

    private static <T> Stream<T> asStream(Iterable<T> from) {
        return StreamSupport.stream(from.spliterator(), false);
    }

    /**
     * A collector to create a {@link Map} from a {@link Stream} of {@link Pair}s.
     *
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> Collector<Pair<S, T>, ?, Map<S, T>> toMap() {
        return Collectors.toMap(Pair::getLeft, Pair::getRight);
    }

    public static <T> Collection<T> subtract(final Collection<T> subtrahend, Collection<T> minuend) {
        Objects.requireNonNull(subtrahend);
        Objects.requireNonNull(minuend);

        List<T> difference = new ArrayList<>(subtrahend);
        difference.removeAll(minuend);
        return difference;
    }

    public static <T> Set<T> subtract(Set<T> subtrahend, Set<T> minuend) {
        Set<T> difference = new HashSet<>(subtrahend);
        difference.removeAll(minuend);
        return difference;
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
