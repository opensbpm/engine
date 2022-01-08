package org.opensbpm.engine.api.instance;

import java.util.List;

public interface IsAttributesContainer {

    String getName();

    List<AttributeSchema> getAttributes();
}
