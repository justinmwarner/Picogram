package com.picogram.awesomeness;

public class HistoryChange {
	public int position;
	char oldCharacter;

	public HistoryChange(int p, char oc) {
		position = p;
		oldCharacter = oc;
	}

	public String toString() {
		return "Pos: " + position + " OC: " + oldCharacter;
	}
}
