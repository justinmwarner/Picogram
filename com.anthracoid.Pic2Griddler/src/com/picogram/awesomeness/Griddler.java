
package com.picogram.awesomeness;

public class Griddler {
    private String id, status, name, diff, rank, author, width, height, solution, current,
            numberOfColors, colors;

    public Griddler() {

    }

    // Constructor for test.
    public Griddler(final int i) {
        this.id = "" + i;
        this.status = "" + 0;
        this.name = "Name: " + i;
    }

    public Griddler(final String id, final String status, final String name,
            final String difficulty, final String rank,
            final String author, final String width, final String height, final String solution,
            final String current) {
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

    public String getAuthor() {
        return this.author;
    }

    public String getColors() {
        return this.colors;
    }

    public String getCurrent() {
        return this.current;
    }

    public String getDiff() {
        return this.diff;
    }

    public String getHeight() {
        return this.height;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getNumberOfColors() {
        return this.numberOfColors;
    }

    public String getRank() {
        return this.rank;
    }

    public String getSolution() {
        return this.solution;
    }

    public String getStatus() {
        return this.status;
    }

    public String getWidth() {
        return this.width;
    }

    public void setColors(final String colors) {
        this.colors = colors;
    }

    public void setNumberOfColors(final String numberOfColors) {
        this.numberOfColors = numberOfColors;
    }

}
