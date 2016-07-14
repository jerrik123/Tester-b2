package com.enumeration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class EnumIterator {

	public static void main(String[] args) {
		Collection<Suit> suitCollection = Arrays.asList(Suit.values());
		Collection<Rank> rankCollection = Arrays.asList(Rank.values());
		for(Iterator<Suit> suits = suitCollection.iterator();suits.hasNext();){
			Suit suit = suits.next();
			for(Iterator<Rank> ranks = rankCollection.iterator();ranks.hasNext();){
				System.out.println("suit=" + suit + " ,rank=" + ranks.next());
			}
		}
	}

}
