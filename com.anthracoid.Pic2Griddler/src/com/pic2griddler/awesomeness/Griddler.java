
package com.pic2griddler.awesomeness;

public class Griddler {
    private String id, status, name, diff, rank, author, width, height, solution, current;

    public Griddler() {

    }

    // Constructor for test.
    public Griddler(int i) {
        this.id = "" + i;
        this.status = "" + 0;
        this.name = "Name: " + i;
    }

    public Griddler(String id, String status, String name, String difficulty, String rank,
            String author, String width, String height, String solution, String current) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.diff = difficulty;
        this.rank = rank;
        this.author = author;
        this.width = width;
        this.height = height;
        this.current = current;
        this.solution = solution;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDiff() {
        return diff;
    }

    public String getRank() {
        return rank;
    }

    public String getAuthor() {
        return author;
    }

    public String getHeight() {
        return height;
    }

    public String getWidth() {
        return width;
    }

    public String getSolution() {
        return solution;
    }

    public String getCurrent() {
        return current;
    }

}
