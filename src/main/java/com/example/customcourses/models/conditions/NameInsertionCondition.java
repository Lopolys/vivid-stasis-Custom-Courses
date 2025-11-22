package com.example.customcourses.models.conditions;

import com.example.customcourses.utils.DataInitializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class NameInsertionCondition implements TitleCondition{
    private String selectedTheme;
    private String userName;

    public NameInsertionCondition() {}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSelectedTheme() {
        return selectedTheme;
    }

    public void setSelectedTheme(String selectedTheme) {
        this.selectedTheme = selectedTheme;
    }

    @Override
    public boolean isSatisfied() throws Exception {
        Path userInfo = DataInitializer.getDataDirectory().resolve("userprefs.json");
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> user = mapper.readValue(new File(userInfo.toString()), new TypeReference<>() {});

        if (!"any".equalsIgnoreCase(selectedTheme)) {
            return (selectedTheme.equals(user.get("theme")) && userName.equals(user.get("userName")));
        }
        else {
            return userName.equals(user.get("userName"));
        }
    }
}
