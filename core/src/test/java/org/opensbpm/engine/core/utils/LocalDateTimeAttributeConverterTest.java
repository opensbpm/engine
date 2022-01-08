package org.opensbpm.engine.core.utils;

import org.opensbpm.engine.core.utils.LocalDateTimeAttributeConverter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author stefan
 */
public class LocalDateTimeAttributeConverterTest {

    @Test
    public void testConvertToDatabaseColumn() {
        // given
        LocalDateTime localDateTime = LocalDateTime.now();

        // when
        Timestamp timestamp = new LocalDateTimeAttributeConverter().convertToDatabaseColumn(localDateTime);

        // then
        assertThat("timestamp must not be null", timestamp, is(notNullValue()));
    }

    @Test
    public void testConvertToEntityAttribute() {
        // given
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // when
        LocalDateTime localDateTime = new LocalDateTimeAttributeConverter().convertToEntityAttribute(timestamp);

        // then
        assertThat("localDateTime must not be null", localDateTime, is(notNullValue()));
    }

}
