package com.sgcib.github.api.json;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.stream.Stream;

@Data
public class User implements Serializable {

    private String login;

    private long id;

    private String type;

    public enum Type {
        USER("User"), ORGANIZATION("Organization"), NONE(StringUtils.EMPTY);

        @Getter
        private String value;

        Type(String value) {
            this.value = value;
        }

        public static Type of(String value) {
            return Stream.of(Type.values()).
                    filter(type -> type.value.equals(value)).
                    findFirst().
                    orElse(NONE);
        }
    }
}
