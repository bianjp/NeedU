var async = require('async');
var db = require('../lib/db').getConnection();

var inform = function(notification, callback){
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

var informNewHelp = function(help){
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
          inform(notifications, callback);
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

var getUserName = function(userId, callback){
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

var getHelp = function(helpId, callback){
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

var informNewComment = function(comment){
  async.parallel({
    authorName: function(callback){
      getUserName(comment.createdBy, callback);
    },
    help: function(callback){
      getHelp(comment.helpId, callback);
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
      inform(notification, function(err){
        if (err){
          console.log('Failed while inform new comment');
        }
      });
    }
  });
};

var informNewReply = function(comment){
  getUserName(comment.createdBy, function(err, authorName){
    if (err){
      console.log('Failed to get authorName while informing new reply');
    }
    else {
      var notification = {
        createdAt: comment.createdAt,
        userId: comment.replyTo,
        type: 3,
        authorId: comment.createdBy,
        authorName: authorName,
        helpId: comment.helpId,
        commentId: comment._id,
        commentContent: comment.content
      };

      inform(notification, function(err){
        if (err){
          console.log('Failed while informing new reply');
        }
      });
    }
  });
};

var informNewConcerner = function(concernerId, userId){
  getUserName(concernerId, function(err, name){
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
      inform(notification, function(err){
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