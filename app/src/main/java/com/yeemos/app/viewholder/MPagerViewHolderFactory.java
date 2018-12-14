package com.yeemos.app.viewholder;

import com.yeemos.app.utils.Constants;

public class MPagerViewHolderFactory {
	private static Constants.MPagerListMode mode;

	static public MPagerViewHolder<?> getViewHolder(Constants.MPagerListMode mode) {
		MPagerViewHolderFactory.mode = mode;
		switch (mode) {
		case MPagerListMode_Users:
		case MPagerListMode_AddressBook:
		case MPagerListMode_Facebook:
		case MPagerListMode_Search:
			return new UserFollowSearchHolder();
		case MPagerListMode_HashTags:
			return new HashTagsSearchHolder();
		default:
			return null;
		}
		
	}
}
