package com.pic2griddler.awesomeness;

public class Griddler
{
	private String id, status, name, diff, rank, author, width, height, solution, current;

	public Griddler()
	{

	}

	public Griddler(String i, String s, String n, String d, String r, String a, String w, String h, String sol, String c)
	{
		this.id = i;
		this.status = s;
		this.name = n;
		this.diff = d;
		this.rank = r;
		this.author = a;
		this.width = w;
		this.height = h;
		this.current = c;
		this.solution = sol;
	}

	public String getId()
	{
		return id;
	}

	public String getStatus()
	{
		return status;
	}

	public String getName()
	{
		return name;
	}

	public String getDiff()
	{
		return diff;
	}

	public String getRank()
	{
		return rank;
	}

	public String getAuthor()
	{
		return author;
	}

	public String getHeight()
	{
		return height;
	}

	public String getWidth()
	{
		return width;
	}

	public String getSolution()
	{
		return solution;
	}

	public String getCurrent()
	{
		return current;
	}

}
