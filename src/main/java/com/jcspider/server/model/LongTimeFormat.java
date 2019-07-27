package com.jcspider.server.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: Gosin
 * @Date: 2019-07-27 18:51
 */
@Component
public class LongTimeFormat {

    public class Serialize extends JsonSerializer<Long> {
        @Override
        public void serialize(Long aLong, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
            jsonGenerator.writeString(sdf.format(new Date(aLong)));
        }
    }

}
