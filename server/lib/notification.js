var async = require('async');

var inform = function(db, notification, callback){
  async.waterfall([
    function(callback){
      db.collection('notifications', callback);
    },

    function(col, callback){
      col.insert(notification, callback);
    }
  ],

  function(err, items){
    if (err || !items.length){
      if (callback){
        callback(err);
      }
      else {
        console.log('Failed to insert notification');
      }
    }
    else{
      if (callback){
        callback(null);
      }
    }
  });
};

var informNewHelp = function(db, help){
  async.waterfall([
    function(callback){
      db.collection('users', callback);
    },

    function(col, callback){
      col.findOne({_id: help.createdBy}, callback);
    },

    function(item, callback){
      if (!item){
        console.log('Failed to get user in informNewHelp');
      }
      else {
        var authorName = item.profile.name;
        var concerners = item.concernedBy;
        if (concerners.length){
          var notifications = [];
          for (var i = 0; i < concerners.length; i++){
            notifications[i] = {
              createdAt: help.createdAt,
              userId: concerners[i],
              type: 1,
              authorId: help.createdBy,
              authorName: authorName,
              helpId: help._id,
              helpTitle: help.title
            };
          }
          inform(db, notifications, callback);
        }
      }
    }
  ],

  function(err){
    if (err){
      console.log('Failed to inform new help');
    }
  });
};

var getUserName = function(db, userId, callback){
  async.waterfall([
    function(callback){
      db.collection('users', callback);
    },

    function(col, callback){
      col.findOne({_id: userId}, callback);
    }
  ],

  function(err, item){
    callback(err || null, item ? item.profile.name : null);
  });
};

var getHelp = function(db, helpId, callback){
  async.waterfall([
    function(callback){
      db.collection('helps', callback);
    },

    function(col, callback){
      col.findOne({_id: helpId}, callback);
    }
  ],

  function(err, item){
    callback(err || null, item || null);
  });
};

var informNewComment = function(db, comment){
  async.parallel({
    authorName: function(callback){
      getUserName(db, comment.createdBy, callback);
    },
    help: function(callback){
      getHelpTitle(db, comment.helpId, callback);
    }
  },

  function(err, result){
    if (err){
      console.log('Failed to get user name or help title in informNewComment');
    }
    else {
      var notification = {
        createdAt: comment.createdAt,
        userId: result.help.createdBy,
        type: 2,
        authorId: comment.createdBy,
        authorName: result.authorName,
        helpId: comment.helpId,
        helpTitle: result.help.title,
        commentId: comment._id,
        commentContent: comment.content
      };
      inform(db, notification, function(err){
        if (err){
          console.log('Failed while inform new comment');
        }
      });
    }
  });
};


var getCommenter = function(db, commentId, callback){
  async.waterfall([
    function(callback){
      db.collection('comments', callback);
    },

    function(col, callback){
      col.findOne({_id: commentId}, callback);
    }
  ],

  function(err, item){
    callback(err || null, item ? item.createdBy : null);
  });
};


var informNewReply = function(db, comment){
  async.parallel({
    authorName: function(callback){
      getUserName(db, comment.createdBy, callback);
    },
    userId: function(callback){ //被回复者
      getCommenter(db, comment.commentId, callback);
    }
  },

  function(err, result){
    var notification = {
      createdAt: comment.createdAt,
      userId: result.userId,
      type: 3,
      authorId: comment.createdBy,
      authorName: result.authorName,
      commentId: comment._id,
      commentContent: comment.content
    };
    inform(db, notification, function(err){
      if (err){
        console.log('Failed while inform new reply');
      }
    });
  });
};

var informNewConcerner = function(db, concernerId, userId){
  getUserName(db, concernerId, function(err, name){
    if (err){
      console.log('Failed while informing new concerner');
    }
    else {
      var notification = {
        createdAt: new Date(),
        userId: userId,
        followerId: concernerId,
        followerName: name
      };
      inform(db, notification, function(err){
        if (err){
          console.log('Failed while informing new concerner');
        }
      });
    }
  });
};

module.exports = {
  informNewHelp: informNewHelp,
  informNewComment: informNewComment,
  informNewReply: informNewReply,
  informNewConcerner: informNewConcerner
};