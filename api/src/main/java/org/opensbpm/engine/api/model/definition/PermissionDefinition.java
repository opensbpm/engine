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
package org.opensbpm.engine.api.model.definition;

import java.util.List;
import javax.xml.bind.annotation.XmlType;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.FieldDefinition;

/**
 * Definition of a Permission for a {@link FieldDefinition} in a
 * {@link StateDefinition}. A Permission can be one of {@link Permission#READ}
 * or {@link Permission#WRITE} and can be {@link #mandatory}.
 *
 */
public interface PermissionDefinition {

    ObjectDefinition getObjectDefinition();

    List<AttributePermissionDefinition> getAttributePermissions();

    public interface AttributePermissionDefinition {

        AttributeDefinition getAttribute();

        Permission getPermission();

        boolean isMandatory();

    }

    public interface NestedPermissionDefinition
            extends AttributePermissionDefinition {

        List<AttributePermissionDefinition> getAttributePermissions();

    }

    public interface ToOnePermission extends NestedPermissionDefinition {

    }

    public interface ToManyPermission extends NestedPermissionDefinition {

    }

    @XmlType
    public enum Permission {
        READ, WRITE
    }

}
