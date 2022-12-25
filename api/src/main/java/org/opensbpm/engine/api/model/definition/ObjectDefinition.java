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
package org.opensbpm.engine.api.model.definition;

import java.util.List;
import java.util.Optional;
import org.opensbpm.engine.api.model.FieldType;

/**
 * Definition of an document in SPM
 *
 */
public interface ObjectDefinition {

    /**
     * Name of a document as valid Java identifier.
     */
    String getName();

    /**
     * The visible representation of a document.
     */
    String getDisplayName();

    /**
     * The list of attributes of this document.
     *
     */
    List<AttributeDefinition> getAttributes();

    /**
     * Definition of an attribute of an document.
     */
    interface AttributeDefinition {

        /**
         * Name of an attribute as valid Java identifier.
         */
        String getName();
        
        /**
         * Script to determine the default value of the attribute
         * @return 
         */
        Optional<String> getDefaultValue();

        <T> T accept(AttributeDefinitionVisitor<T> visitor);
    }

    interface AttributeDefinitionVisitor<T> {

        T visitField(FieldDefinition fieldDefinition);

        T visitReference(ReferenceDefinition referenceDefinition);

        T visitToOne(ToOneDefinition toOneDefinition);

        T visitToMany(ToManyDefinition toManyDefinition);

    }

    /**
     * Definition of an field.
     */
    interface FieldDefinition extends AttributeDefinition {

        FieldType getFieldType();

        boolean isIndexed();

        ObjectDefinition getAucompleteObject();

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitField(this);
        }

    }

    /**
     * Definition of an referencing attribute.
     */
    interface ReferenceDefinition extends NestedAttribute {

        ObjectDefinition getObjectDefinition();

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitReference(this);
        }

    }

    /**
     * Definition of an nested attribute.
     */
    interface NestedAttribute extends AttributeDefinition {

        List<AttributeDefinition> getAttributes();
    }

    /**
     * Definition of an single nested attribute.
     */
    interface ToOneDefinition extends NestedAttribute {

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitToOne(this);
        }
    }

    /**
     * Definition of an list nested attribute.
     */
    interface ToManyDefinition extends NestedAttribute {

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitToMany(this);
        }

    }

}
