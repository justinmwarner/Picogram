package com.pic2griddler.awesomeness;

public class Griddler
{
	private String id, status, name, diff, rank, info, author;

	public Griddler()
	{

	}

	public Griddler(String i, String s, String n, String d, String r, String in, String a)
	{
		this.id = i;
		this.status = s;
		this.name = n;
		this.diff = d;
		this.rank = r;
		this.info = in;
		this.author = a;
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

	public String getInfo()
	{
		return info;
	}

	public String getAuthor()
	{
		return author;
	}

}
