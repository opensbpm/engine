package org.opensbpm.engine.api.instance;

import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.opensbpm.engine.api.model.FieldType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AttributeSchemaVisitorTest {

    @Test
    public void testSimple() {
        //arrange
        SimpleAttributeSchema attributeSchema = SimpleAttributeSchema.of(0L, "name", FieldType.DATE);

        //act
        Optional<AttributeSchema> optional = attributeSchema.accept(AttributeSchemaVisitor.simple());

        //assert
        assertThat(optional.get(), is(attributeSchema));
    }

    @Test
    public void testNested() {
        //arrange
        NestedAttributeSchema attributeSchema = NestedAttributeSchema.create(0L, "name", Collections.emptyList());

        //act
        Optional<NestedAttributeSchema> optional = attributeSchema.accept(AttributeSchemaVisitor.nested());

        //assert
        assertThat(optional.get(), is(attributeSchema));
    }

    @Test
    public void testIndexed() {
        //arrange
        IndexedAttributeSchema attributeSchema = IndexedAttributeSchema.create(0L, "name", Collections.emptyList());

        //act
        Optional<IndexedAttributeSchema> optional = attributeSchema.accept(AttributeSchemaVisitor.indexed());

        //assert
        assertThat(optional.get(), is(attributeSchema));
    }

}
