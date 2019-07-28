package com.jcspider.server.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: Gosin
 * @Date: 2019-07-27 18:51
 */
@Component
public class LongTimeFormat {
    private static final Logger LOGGER = LoggerFactory.getLogger(LongTimeFormat.class);

    public class Serialize extends JsonSerializer<Long> {
        @Override
        public void serialize(Long aLong, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            jsonGenerator.writeString(sdf.format(new Date(aLong)));
        }
    }

    public class Deserialize extends JsonDeserializer<Long> {

        @Override
        public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(jsonParser.getText()).getTime();
            } catch (ParseException e) {
                LOGGER.error("parse error",e);
                return null;
            }
        }
    }

}
