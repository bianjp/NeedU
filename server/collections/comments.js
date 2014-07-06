/*
	comments
	{
		_id: ObjectId,
		createdAt: Date,
		createdBy: ObjectId,
		helpId: ObjectId,
		commentId: ObjectId,	//所回复评论。若是对求助信息的直接评论，则为Null
		content: string,
		secret: bool,			//悄悄话
		thanked: bool			//被感谢
	}
*/