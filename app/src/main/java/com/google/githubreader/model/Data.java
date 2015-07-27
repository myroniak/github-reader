package com.google.githubreader.model;

import java.io.Serializable;

/**
 * Created by Roman on 22.05.2015.
 */
public class Data implements Serializable {


    private int id;
    private String countFork;
    private String countStar;

    private String name;
    private String language;

    public Data(String name, String language, String countFork, String countStar) {
        this.name = name;
        this.language = language;
        this.countFork = countFork;
        this.countStar = countStar;

    }

    public int getId() {
        return id;
    }

    public String getCountStar() {
        return countStar;
    }

    public String getCountFork() {
        return countFork;
    }

    public String getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }


}
