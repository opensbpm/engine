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

import org.opensbpm.engine.api.model.FieldType;
import java.util.List;

public interface ObjectDefinition {

    String getName();
    
    String getDisplayName();

    List<AttributeDefinition> getAttributes();

    public static interface AttributeDefinition {

        String getName();

        abstract <T> T accept(AttributeDefinitionVisitor<T> visitor);
    }

    public interface AttributeDefinitionVisitor<T> {

        T visitField(FieldDefinition fieldDefinition);

        T visitReference(ReferenceDefinition referenceDefinition);
        
        T visitToOne(ToOneDefinition toOneDefinition);

        T visitToMany(ToManyDefinition toManyDefinition);

    }

    public interface FieldDefinition extends AttributeDefinition {

        FieldType getFieldType();
        
        boolean isIndexed();
        
        ObjectDefinition getAucompleteObject();

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitField(this);
        }

    }

    public interface ReferenceDefinition extends AttributeDefinition {

        ObjectDefinition getObjectDefinition();

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitReference(this);
        }

    }

    public interface NestedAttribute extends AttributeDefinition {

        List<AttributeDefinition> getAttributes();
    }

    public interface ToOneDefinition extends NestedAttribute {

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitToOne(this);
        }
    }

    public interface ToManyDefinition extends NestedAttribute {

        @Override
        default <T> T accept(AttributeDefinitionVisitor<T> visitor) {
            return visitor.visitToMany(this);
        }

    }

}
