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
package org.opensbpm.engine.api.model.definition;

import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

import org.opensbpm.engine.api.model.definition.ParametersAdapter.ParametersWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.w3c.dom.Element;

public class ParametersAdapter extends XmlAdapter<ParametersWrapper, Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(ParametersWrapper v) throws Exception {
        return v.toMap();
    }

    @Override
    public ParametersWrapper marshal(Map<String, String> m) throws Exception {
        ParametersWrapper wrapper = new ParametersWrapper();
        if (m != null) {
            for (Map.Entry<String, String> entry : m.entrySet()) {
                wrapper.addEntry(new JAXBElement<>(new QName(entry.getKey()), String.class, entry.getValue()));
            }
        }
        return wrapper;
    }

    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ParametersWrapper {

        @XmlAnyElement
        private List<JAXBElement<String>> parameters = new ArrayList<>();

        public ParametersWrapper() {
            //JAXB-Constructor
        }

        public List<JAXBElement<String>> getParameters() {
            return emptyOrUnmodifiableList(parameters);
        }

        public void setParameters(List<JAXBElement<String>> parameters) {
            this.parameters = parameters;
        }

        /**
         * Only use {@link #addEntry(JAXBElement)} and {{@link #addEntry(String, String)} when this
         * <code>MapWrapper</code> instance is created by yourself * (instead of through unmarshalling).
         *
         * @param key map key
         * @param value map value
         */
        public void addEntry(String key, String value) {
            addEntry(new JAXBElement<>(new QName(key), String.class, value));
        }

        public void addEntry(JAXBElement<String> prop) {
            parameters = lazyAdd(parameters, prop);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("parameters", toMap())
                    .toString();
        }

        public Map<String, String> toMap() {
            //Note: Due to type erasure, you cannot use parameters.stream() directly when unmashalling is used..
            List<?> props = parameters;
            return props.stream()
                    .collect(Collectors.toMap(ParametersWrapper::extractLocalName, ParametersWrapper::extractTextContent));
        }

        /**
         * Extract local name from <code>obj</code>, whether it's javax.xml.bind.JAXBElement or org.w3c.dom.Element;
         *
         * @param obj
         * @return
         */
        @SuppressWarnings("unchecked")
        private static String extractLocalName(Object obj) {
            Map<Class<?>, Function<? super Object, String>> strFuncs = new HashMap<>();
            strFuncs.put(JAXBElement.class, jaxb -> ((JAXBElement<String>) jaxb).getName().getLocalPart());
            strFuncs.put(Element.class, ele -> ((Element) ele).getLocalName());
            return extractPart(obj, strFuncs).orElse("");
        }

        /**
         * Extract text content from <code>obj</code>, whether it's javax.xml.bind.JAXBElement or org.w3c.dom.Element;
         *
         * @param obj
         * @return
         */
        @SuppressWarnings("unchecked")
        private static String extractTextContent(Object obj) {
            Map<Class<?>, Function<? super Object, String>> strFuncs = new HashMap<>();
            strFuncs.put(JAXBElement.class, jaxb -> ((JAXBElement<String>) jaxb).getValue());
            strFuncs.put(Element.class, ele -> ((Element) ele).getTextContent());
            return extractPart(obj, strFuncs).orElse("");
        }

        /**
         * Check class type of <code>obj</code> according to types listed in <code>strFuncs</code> keys, then extract
         * some string part from it according to the extract function specified in <code>strFuncs</code> values.
         *
         * @param obj
         * @param strFuncs
         * @return
         */
        private static <T, V> Optional<V> extractPart(T obj, Map<Class<?>, Function<? super T, V>> strFuncs) {
            for (Entry<Class<?>, Function<? super T, V>> entry : strFuncs.entrySet()) {
                if (entry.getKey().isInstance(obj)) {
                    return Optional.of(entry.getValue().apply(obj));
                }
            }
            return Optional.empty();

        }
    }
}
