/*
	users
	{
		_id: ObjectId,
		username: string,	//学号
		password: {
			identity: string,	// = sha1(sha1(原始密码) + salt)
			salt: string
		},
		createdAt: Date,
		role: 'admin',			//只有管理员有这个字段
		profile: {
			school: string,		//学院
			major: string,		//专业
			schoolYear: int,	//入学年份
			name: string,
			gender: 'male'/'female',
			photo: string,		//图片的路径
			birthday: Date,
			phone: string,
			wechat: string,
			QQ: string,
			description: string	//个人描述
		},
		concernedBy: [ObjectId]	//关注者	有动态时方便通知
		concerns: [ObjectId]	//关注的
	}

*/