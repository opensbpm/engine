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
package org.opensbpm.engine.xmlmodel;

import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.opensbpm.engine.xmlmodel.processmodel.ObjectFactory;
import org.opensbpm.engine.xmlmodel.processmodel.ProcessType;
import org.xml.sax.SAXException;

public class ProcessModel {

    private final JAXBContext jaxbContext;

    public ProcessModel() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(ProcessType.class);
    }

    public void marshal(ProcessDefinition processDefinition, OutputStream os) throws JAXBException {
        ProcessType processType = new ProcessDefinitionConverter().convert(processDefinition);
        getMarshaller().marshal(new ObjectFactory().createProcess(processType), os);
    }

    public void marshal(ProcessDefinition processDefinition, Writer writer) throws JAXBException {
        ProcessType processType = new ProcessDefinitionConverter().convert(processDefinition);
        getMarshaller().marshal(new ObjectFactory().createProcess(processType), writer);
    }

    private Marshaller getMarshaller() throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        if (false) {
            //TODO disabled for now; it doesn't work as expected
            marshaller.setSchema(createSchema());
        }
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    public ProcessDefinition unmarshal(InputStream is) throws JAXBException {
        JAXBElement<ProcessType> element = (JAXBElement<ProcessType>) getUnmarshaller().unmarshal(is);
        return new ProcessTypeConverter().convert(element.getValue());
    }

    public ProcessDefinition unmarshal(Reader reader) throws JAXBException {
        JAXBElement<ProcessType> element = (JAXBElement<ProcessType>) getUnmarshaller().unmarshal(reader);
        return new ProcessTypeConverter().convert(element.getValue());
    }

    private Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if (false) {
            //TODO disabled for now; it doesn't work as expected
            unmarshaller.setSchema(createSchema());
        }
        return unmarshaller;
    }

    private Schema createSchema() throws JAXBException {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            return sf.newSchema(new StreamSource(loadXsd()));
        } catch (SAXException ex) {
            throw new JAXBException(ex.getMessage(), ex);
        }
    }

    private static InputStream loadXsd() {
        return ProcessModel.class.getResourceAsStream("/org/opensbpm/engine/xmlmodel/processmodel.xsd");
    }

}
