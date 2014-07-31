var async = require('async');
var db = require('../lib/db').getConnection();
var User = require('./user');
var Help = require('./help');
var Comment = require('./comment');

var getUserName = function(userId, callback){
  User.findOne({_id: userId}, function(err, item){
    callback(err, item && item.profile.name);
  });
};

module.exports = {
  findOne: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('notifications', callback);
      },

      function(col, callback){
        col.findOne(selector, callback);
      }
    ], callback);
  },

  find: function(selector, options, callback){
    async.waterfall([
      function(callback){
        db.collection('notifications', callback);
      },

      function(col, callback){
        col.find(selector, options).toArray(callback);
      }
    ], callback);
  },

  insert: function(doc, callback){
    async.waterfall([
      function(callback){
        db.collection('notifications', callback);
      },

      function(col, callback){
        col.insert(doc, callback);
      }
    ], callback);
  },

  update: function(selector, updater, callback){
    async.waterfall([
      function(callback){
        db.collection('notifications', callback);
      },

      function(col, callback){
        col.update(selector, updater, callback);
      }
    ], callback);
  },

  remove: function(selector, callback){
    async.waterfall([
      function(callback){
        db.collection('notifications', callback);
      },

      function(col, callback){
        col.remove(selector, callback);
      }
    ], callback);
  },

  informNewHelp: function(help){
    var Notification = this;
    async.waterfall([
      function(callback){
        User.findOne({_id: help.createdBy}, callback);
      },

      function(item, callback){
        if (!item){
          callback(true);
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
            Notification.insert(notifications, callback);
          }
        }
      }
    ],

    function(err){
      if (err){
        console.log('Failed to inform new help');
      }
    });
  },

  informNewComment: function(comment){
    var Notification = this;
    async.parallel({
      authorName: function(callback){
        getUserName(comment.createdBy, callback);
      },
      help: function(callback){
        Help.findOne({_id: comment.helpId}, callback);
      }
    },

    function(err, result){
      if (err){
        callback(err);
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
        Notification.insert(notification, function(err){
          if (err){
            console.log('Failed while inform new comment');
          }
        });
      }
    });
  },

  informNewReply: function(comment){
    getUserName(comment.createdBy, function(err, authorName){
      if (err){
        callback(err);
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

        Notification.insert(notification, function(err){
          if (err){
            console.log('Failed while informing new reply');
          }
        });
      }
    });
  },

  informNewConcerner: function(concernerId, userId){
    getUserName(concernerId, function(err, name){
      if (err){
        callback(err);
      }
      else {
        var notification = {
          createdAt: new Date(),
          userId: userId,
          followerId: concernerId,
          followerName: name
        };
        Notification.insert(notification, function(err){
          if (err){
            console.log('Failed while informing new concerner');
          }
        });
      }
    });
  }
};
