/*
 * CollectionUtils.java
 *
 * Created on 28.04.2020,14:52:34
 *
 */
package org.opensbpm.engine.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author stefan
 */
public final class CollectionUtils {

    /**
     * Create a unmodifiable {@link Collection} of the given collection. If the the given collection is
     * <code>null</code> a empty and unmodifiable {@link Collection} will be returned
     *
     * @param <T> Type of collection
     * @param collection nullable collection
     * @return a unmodifiable collection or a empty unmodifiable collection in case of <code>null</code>
     */
    public static <T> Collection<T> readonlyCollection(Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(t -> Collections.unmodifiableCollection(t))
                .orElse(Collections.emptyList());
    }

    /**
     * Adds a new element to the given list and returns the modified list. If the given list is <code>null</code> a new
     * empty list will be created.
     *
     * @param <T> Type of list
     * @param list nullable list
     * @param element
     * @return a new list containing the given element
     */
    public static <T> List<T> add(List<T> list, T element) {
        Objects.requireNonNull(element, "element must not be null");
        list = Optional.ofNullable(list)
                .orElse(new ArrayList<>());
        list.add(element);
        return list;
    }

    private CollectionUtils() {
    }

}
