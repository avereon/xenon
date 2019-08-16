package com.avereon.xenon.update;

import java.util.Comparator;

public class RepoStateComparator implements Comparator<RepoState> {

	public enum Field {
		NAME,
		RANK,
		REPO
	}

	private Field field;

	public RepoStateComparator( Field field ) {
		this.field = field;
	}

	@Override
	public int compare( RepoState state1, RepoState state2 ) {
		switch( field ) {
			case RANK: {
				return state1.getRank() - state2.getRank();
			}
			case REPO: {
				return state1.getUrl().compareTo( state2.getUrl() );
			}
			default: {
				return state1.getName().compareTo( state2.getName() );
			}
		}
	}

}
