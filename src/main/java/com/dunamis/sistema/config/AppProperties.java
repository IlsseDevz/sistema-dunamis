package com.dunamis.sistema.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Church church = new Church();
    private BibleSchool bibleSchool = new BibleSchool();

    public Church getChurch() {
        return church;
    }

    public void setChurch(Church church) {
        this.church = church;
    }

    public BibleSchool getBibleSchool() {
        return bibleSchool;
    }

    public void setBibleSchool(BibleSchool bibleSchool) {
        this.bibleSchool = bibleSchool;
    }

    public static class Church {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class BibleSchool {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
