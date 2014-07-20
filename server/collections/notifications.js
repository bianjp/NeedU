/*
	notifications
	{
		_id: ObjectId,
		createdAt: Date,
		userId: ObjectId,   //通知用户
		type: int,			   	//通知类型
    ....
	}

  type: 1                 //关注用户发布求助
  authorId: ObjectID,
  authorName: string
  helpId: ObjectID
  helpTitle: string

  type: 2                 //求助被评论
  authorId: ObjectID,
  authorName: string,
  helpId: ObjectID,
  helpTitle: string,
  commentId: ObjectID,
  commentContent: string

  type: 3                 //评论被回复
  authorId: ObjectID,
  authorName: string,
  helpId: ObjectID,
  commentId: ObjectID,
  commentContent: string

  type: 4                 //被关注
  followerId: ObjectID,
  followerName: string
*/